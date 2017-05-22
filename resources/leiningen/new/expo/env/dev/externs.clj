(ns externs
  (:require [cljs.compiler.api :as compiler]
            [cljs.analyzer.api :as analyzer]
            [cljs.analyzer :as ana]
            [clojure.walk :refer [prewalk]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cljs.env :as env]
            [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.tagged-literals :as tags])
  (:import (clojure.lang LineNumberingPushbackReader)))

;; Idea from https://gist.github.com/Chouser/5796967

;; TODO (NAMESPACE/MODULE.method ...args) or NAMESPACE/MODULE.field not work
;; For example, (ui/Facebook.logInWithReadPermissionsAsync ...args)
;; or ui/Permissions.REMOTE_NOTIFICATIONS,
;; we already know logInWithReadPermissionsAsync is `invoke' op
;; and REMOTE_NOTIFICATIONS is a property, need to dig deeper to the ast

;; TODO ana/analyze is slow

(defonce cenv (analyzer/empty-state))

(defn compile-project
  [src target]
  (analyzer/with-state cenv
    (compiler/with-core-cljs
      (compiler/compile-root src target))))

(defn get-namespaces
  []
  (:cljs.analyzer/namespaces @cenv))

(defn get-namespace
  ([]
   (get-namespace ana/*cljs-ns*))
  ([k]
   (get (get-namespaces) k)))

(defn get-alias
  [ns]
  (apply merge
    ((juxt :requires :require-macros)
     (get-namespace ns))))

(defn print-ast [ast]
  (pprint  ;; pprint indents output nicely
   (prewalk ;; rewrite each node of the ast
    (fn [x]
      (if (map? x)
        (select-keys x [:children :name :form :op]) ;; return selected entries of each map node
        x))  ;; non-map nodes are left unchanged
    ast)))

(defn get-ns
  [s]
  (some->>
   (re-find #"\(ns[\s]+([^\s]+)" s)
   (last)
   (symbol)))

(defn read-file
  [filename]
  (try
    (let [form-str (slurp filename)
          current-ns (get-ns form-str)
          reader (string-push-back-reader form-str)
          endof (gensym)]
      (binding [r/*read-eval* false
                r/*data-readers* tags/*cljs-data-readers*
                r/*alias-map*    (try
                                   (get-alias (ns-name current-ns))
                                   (catch Exception e
                                     {}))]
        (->> #(r/read reader false endof)
             (repeatedly)
             (take-while #(not= % endof))
             (doall))))
    (catch Exception e
      (println e)
      '())))

(defn file-ast
  "Return the ClojureScript AST for the contents of filename. Tends to
  be large and to contain cycles -- be careful printing at the REPL."
  [filename]
  (binding [ana/*cljs-ns* 'cljs.user ;; default namespace
            ana/*cljs-file* filename]
    (mapv
     (fn [form]
       (try (ana/no-warn (ana/analyze (ana/empty-env) form {:cache-analysis true}))
            (catch Exception e
              (prn filename e))))
     (read-file filename))))

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

(defn get-interop-used
  "Return a set of symbols representing the method and field names
  used in interop forms in the given sequence of AST nodes."
  [flat-ast]
  (keep #(let [ret (and (map? %)
                        (when-let [sym (some % [:method :field])]
                          (when-not (str/starts-with? sym "cljs")
                            sym)))]
           (if ret
             ret
             nil)) flat-ast))

(defn externs-for-interop [syms]
  (apply str
    "var DummyClass={};\n"
    (map #(str "DummyClass." % "=function(){};\n")
      syms)))

(defn var-defined?
  "Returns true if the given fully-qualified symbol is known by the
  ClojureScript compiler to have been defined, based on its mutable set
  of namespaces."
  [sym]
  (contains? (let [ns (get (get-namespaces) (symbol (namespace sym)))]
               (merge (:defs ns)
                      (:macros ns)))
             (symbol (name sym))))

(defn get-vars-used
  "Return a set of symbols representing all vars used or referenced in
  the given sequence of AST nodes."
  [requires flat-ast]
  (->> flat-ast
       (filter #(let [ns (-> % :info :ns)]
                  (and (= (:op %) :var)
                       ns
                       (not= ns 'js))))
       (map #(let [sym (-> % :info :name)
                   sym-namespace (get requires (symbol (namespace sym)))
                   sym-name (name sym)]
               (if sym-namespace
                 (symbol (str sym-namespace) sym-name)
                 sym)))))

(defn extern-for-var [[str-namespace symbols]]
  (let [symbols-str (->> symbols
                         (map (fn [sym] (format "%s.%s={};\n" (namespace sym) (name sym))))
                         (apply str))]
    (format "var %s={};\n%s"
            str-namespace symbols-str)))

(defn externs-for-vars [grouped-syms]
  (apply str (map extern-for-var grouped-syms)))

(defn get-undefined-vars [requires flat-ast]
  (->> (get-vars-used requires flat-ast)
       (remove var-defined?)))

(defn get-undefined-vars-and-interop-used [file]
  (let [ast (file-ast file)
        ns-name (:name (first ast))
        ns-requires (:requires (first ast))
        flat-ast (flatten-ast ast)]
    [(get-undefined-vars ns-requires flat-ast)
     (get-interop-used flat-ast)]))

;; copy from https://github.com/ejlo/lein-externs/blob/master/src/leiningen/externs.clj
(defn cljs-file?
  "Returns true if the java.io.File represents a normal Clojurescript source
  file."
  [^java.io.File file]
  (and (.isFile file)
       (.endsWith (.getName file) ".cljs")))

(defn get-source-paths [build-type builds]
  (or
   (when build-type
     (:source-paths
      (or ((keyword build-type) builds)
          (first (filter #(= (name (:id %)) build-type) builds)))))
   ["src" "cljs"]))

(defn -main
  "Generate an externs file"
  []
  ;; TODO configurable
  (println "Start to generate externs...")
  (compile-project (io/file "src") (io/file "target"))

  (let [source-paths ["src" "env/prod"]

        files        (->> source-paths
                          (map io/file)
                          (mapcat file-seq)
                          (filter cljs-file?))
        col          (apply concat (doall (pmap get-undefined-vars-and-interop-used files)))
        vars (->> (take-nth 2 col)
                  (remove empty?)
                  (flatten)
                  (set)
                  (sort)
                  (group-by namespace)
                  ;; remove goog dependencies, need to dig deeper(TODO)
                  (remove (fn [[ns _]] (str/starts-with? ns "goog")))
                  (externs-for-vars))
        interop (->> (take-nth 2 (rest col))
                     (remove empty?)
                     (flatten)
                     (set)
                     (sort)
                     (externs-for-interop))
        result (str vars interop)]
    (spit "js/externs.js" result)
    (println "Generated externs to js/externs.js")

    ;; prevent jvm hang after this task, maybe Clojurescript uses pmap for parallel compilation.
    (shutdown-agents)))
