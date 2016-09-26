(ns leiningen.new.exponent
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "exponent"))

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
             ["figwheel-bridge.js" (render "figwheel-bridge.js" data)]
             ["index.android.js" (render "index.android.js" data)]
             ["index.ios.js" (render "index.ios.js" data)]
             ["main.js" (render "main.js" data)]
             ["package.json" (render "package.json" data)]
             ["project.clj" (render "project.clj" data)]
             ["readme.md" (render "readme.md" data)]
             ["env/dev/env/android/main.cljs" (render "env/dev/env/android/main.cljs" data)]
             ["env/dev/env/ios/main.cljs" (render "env/dev/env/ios/main.cljs" data)]
             ["env/dev/user.clj" (render "env/dev/user.clj" data)]
             ["env/prod/env/android/main.cljs" (render "env/prod/env/android/main.cljs" data)]
             ["env/prod/env/ios/main.cljs" (render "env/prod/env/ios/main.cljs" data)]
             ["src/cljsjs/react.cljs" (render "src/cljsjs/react.cljs" data)]
             ["src/{{sanitized}}/exponent/android/core.cljs"
              (render "src/exponent/android/core.cljs" data)]
             ["src/{{sanitized}}/ios/core.cljs"
              (render "src/exponent/ios/core.cljs" data)]
             ["src/{{sanitized}}/db.cljs"
              (render "src/exponent/db.cljs" data)]
             ["src/{{sanitized}}/handlers.cljs"
              (render "src/exponent/handlers.cljs" data)]
             ["src/{{sanitized}}/subs.cljs"
              (render "src/exponent/subs.cljs" data)]
             ["test/{{sanitized}}/core_test.clj"
              (render "test/exponent/core_test.clj" data)])))
