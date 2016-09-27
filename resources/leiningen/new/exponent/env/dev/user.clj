(ns user
  (:require [figwheel-sidecar.repl-api :as ra]
            [clojure.java.io :as io]
            [clojure.string :as str]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def project-config (->> "project.clj"
                         slurp
                         read-string
                         (drop 1)
                         (apply hash-map)))

(def profiles (:profiles project-config))

(def js-modules (:js-modules project-config))

(def cljs-builds (get-in profiles [:dev :cljsbuild :builds]))

(defn keyword->str [s]
  (str/replace s
               #"::(\S)*"
               (fn [r]
                 (str \" (str/replace (first r) #"::" "") \"))))

(defn write-env-dev
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))
        ip (.getHostAddress (java.net.InetAddress/getLocalHost))]
    (-> "(ns env.dev)\n(def hostname \"%s\")\n(def ip \"%s\")"
        (format
         hostname
         ip)
        ((partial spit "env/dev/env/dev.cljs")))))

(defn rebuild-env-index
  []
  (let [modules (->> (file-seq (io/file "assets"))
                     (filter #(.isFile %))
                     (map (fn [file] (when-let [path (str file)]
                                      (str "../../" path))))
                     (concat js-modules)
                     (distinct))
        modules-map (zipmap
                     (->> modules
                          (map #(str "::" %)))
                     (->> modules
                          (map #(format "(js/require \"%s\")" %))))]


    (try
      (-> "(ns env.index\n  (:require [env.dev :as dev]))\n\n;; undo main.js goog preamble hack\n(set! js/window.goog js/undefined)\n\n(-> (js/require \"figwheel-bridge\")\n    (.withModules %s)\n    (.start \"main\"))\n"
         (format
          (str "#js " (with-out-str (println modules-map))))
         (keyword->str)
         ((partial spit "env/dev/env/index.cljs")))

      (println "Re-generate ./env/dev/env/index.cljs")
      (catch Exception e
        (println "Error: " e)))))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [& build-ids]
  (write-env-dev)
  (rebuild-env-index)
  (ra/start-figwheel!
   {:figwheel-options {}
    :build-ids  (if (seq build-ids )
                  build-ids
                  ["main"])
    :all-builds cljs-builds})
  (ra/cljs-repl))

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(defn -main
  [args]
  (case args
    "--figwheel"
    (start-figwheel)

    "--re-generate"
    (rebuild-env-index)

    (prn "You can run lein figwheel or lein re-generate.")))
