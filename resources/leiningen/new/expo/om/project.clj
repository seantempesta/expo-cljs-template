(defproject {{name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.854"]
                 [org.omcljs/om "1.0.0-beta1" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [react-native-externs "0.1.0"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.14"]]
  :clean-targets ["target/" "main.js"]
  :aliases {"figwheel"        ["run" "-m" "user" "--figwheel"]
            ; TODO: Remove custom extern inference as it's unreliable
            ;"externs"         ["do" "clean"
            ;                   ["run" "-m" "externs"]]
            "rebuild-modules" ["run" "-m" "user" "--rebuild-modules"]
            "prod-build"      ^{:doc "Recompile code with prod profile."}
                              ["with-profile" "prod" "cljsbuild" "once" "main"]}
  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.11"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "env/dev"]
                   :cljsbuild    {:builds [{:id "main"
                                            :source-paths ["src" "env/dev"]
                                            :figwheel     true
                                            :compiler     {:output-to     "target/expo/not-used.js"
                                                           :main          "env.expo.main"
                                                           :output-dir    "target/expo"
                                                           :optimizations :none}}]}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds [{:id "main"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to     "main.js"
                                                         :main          "env.expo.main"
                                                         :output-dir    "target/expo"
                                                         :static-fns    true
                                                         :externs       ["js/externs.js"]
                                                         :infer-externs      true
                                                         :parallel-build     true
                                                         :optimize-constants true
                                                         :optimizations :advanced
                                                         :closure-defines {"goog.DEBUG" false}}}]}}})
