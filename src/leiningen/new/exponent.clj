(ns leiningen.new.exponent
  (:require [leiningen.new.templates :refer [renderer raw-resourcer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "exponent"))
(def raw (raw-resourcer "exponent"))

(defn exponent
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' exponent project.")
    (->files data
             [".babelrc" (render ".babelrc" data)]
             [".gitignore" (render ".gitignore" data)]
             [".hgignore" (render ".hgignore" data)]
             ["LICENSE" (render "LICENSE" data)]
             ["exp.json" (render "exp.json" data)]
             ["js/figwheel-bridge.js" (render "js/figwheel-bridge.js" data)]
             ["js/main.js" (render "js/main.js" data)]
             ["assets/images/cljs.png" (raw "assets/images/cljs.png")]
             ["assets/images/cljs@2x.png" (raw "assets/images/cljs@2x.png")]
             ["assets/images/cljs@3x.png" (raw "assets/images/cljs@3x.png")]
             ["package.json" (render "package.json" data)]
             ["project.clj" (render "project.clj" data)]
             ["readme.md" (render "readme.md" data)]
             ["env/dev/env/main.cljs" (render "env/dev/env/main.cljs" data)]
             ["env/dev/user.clj" (render "env/dev/user.clj" data)]
             ["env/prod/env/main.cljs" (render "env/prod/env/main.cljs" data)]
             ["src/cljsjs/react.cljs" (render "src/cljsjs/react.cljs" data)]
             ["src/{{sanitized}}/core.cljs"
              (render "src/exponent/core.cljs" data)]
             ["src/{{sanitized}}/db.cljs"
              (render "src/exponent/db.cljs" data)]
             ["src/{{sanitized}}/handlers.cljs"
              (render "src/exponent/handlers.cljs" data)]
             ["src/{{sanitized}}/subs.cljs"
              (render "src/exponent/subs.cljs" data)]
             ["test/{{sanitized}}/core_test.clj"
              (render "test/exponent/core_test.clj" data)])))
