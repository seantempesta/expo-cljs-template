(set-env!
 :source-paths   #{"src" "env/dev"}
 :dependencies '[[ajchemist/boot-figwheel "0.5.4-6" :scope "test"] ;; latest release
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [figwheel-sidecar "0.5.4-7" :scope "test"]
                 [react-native-externs "0.0.2-SNAPSHOT" :scope "test"]

                 [org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/clojurescript      "1.9.293"]
                 [reagent "0.6.0-SNAPSHOT" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [re-frame "0.9.1"]
                 [cljs-exponent "0.1.6"]])

(require
 '[boot-figwheel :refer [figwheel cljs-repl]]
 '[cljs.build.api :as b]
 '[user :as user]
 '[externs :as externs])

(require 'boot.repl)
(swap! boot.repl/*default-middleware*
       conj 'cemerick.piggieback/wrap-cljs-repl)

(deftask dev
  "boot dev, then input (cljs-repl)"
  []
  (user/prepare)

  (comp
   (figwheel
    :build-ids  ["main"]
    :all-builds [{:id "main"
                  :source-paths ["src" "env/dev"]
                  :figwheel true
                  :compiler     {:output-to     "not-used.js"
                                 :main          "env.main"
                                 :optimizations :none
                                 :output-dir    "."}}]
    :figwheel-options {:open-file-command "emacsclient"
                       :validate-config false})
   (repl)))

(deftask prod
  []
  (externs/-main)

  (println "Start to compile clojurescript ...")
  (let [start (System/nanoTime)]
    (b/build ["src" "env/prod"]
             {:output-to     "main.js"
              :main          "env.main"
              :output-dir    "target"
              :static-fns    true
              :externs       ["js/externs.js"]
              :parallel-build     true
              :optimize-constants true
              :optimizations :advanced
              :closure-defines {"goog.DEBUG" false}})
    (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds")))
