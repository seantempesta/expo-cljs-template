(ns leiningen.new.exponent
  (:require [leiningen.new.templates :refer [renderer raw-resourcer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "exponent"))
(def raw (raw-resourcer "exponent"))

(defn templates-by-lib
  [lib data]
  (get
   {:om [["project.clj" (render "om/project.clj" data)]
         ["src/{{sanitized}}/core.cljs" (render "om/core.cljs" data)]
         ["src/{{sanitized}}/state.cljs" (render "om/state.cljs" data)]
         ["env/dev/env/main.cljs" (render "om/main_dev.cljs" data)]
         ["src/re_natal/support.cljs" (render "om/support.cljs" data)]]
    :reagent [["project.clj" (render "reagent/project.clj" data)]
              ["src/{{sanitized}}/core.cljs" (render "reagent/core.cljs" data)]
              ["src/{{sanitized}}/db.cljs" (render "reagent/db.cljs" data)]
              ["src/{{sanitized}}/handlers.cljs" (render "reagent/handlers.cljs" data)]
              ["src/{{sanitized}}/subs.cljs" (render "reagent/subs.cljs" data)]
              ["env/dev/env/main.cljs" (render "reagent/main_dev.cljs" data)]]}
   lib))

(defn exponent [name & lib]
  (main/info "Generating fresh Exponent project.")
  (main/info "README.md contains instructions to get you started.")

  (let [data {:name name
              :sanitized (name-to-path name)}
        lib (if-let [lib (first lib)]
              (let [v (keyword (clojure.string/replace lib "+" ""))]
                v)
              :reagent)]
    (->>
     [[".babelrc" (render ".babelrc" data)]
      [".gitignore" (render ".gitignore" data)]
      [".hgignore" (render ".hgignore" data)]
      [".projectile" (render ".projectile" data)]
      ["LICENSE" (render "LICENSE" data)]
      ["exp.json" (render "exp.json" data)]
      ["js/figwheel-bridge.js" (render "js/figwheel-bridge.js" data)]
      ["assets/images/cljs.png" (raw "assets/images/cljs.png")]
      ["assets/images/cljs@2x.png" (raw "assets/images/cljs@2x.png")]
      ["assets/images/cljs@3x.png" (raw "assets/images/cljs@3x.png")]
      ["package.json" (render "package.json" data)]

      ["readme.md" (render "readme.md" data)]
      ["env/dev/user.clj" (render "env/dev/user.clj" data)]
      ["env/prod/env/main.cljs" (render "env/prod/env/main.cljs" data)]
      ["src/cljsjs/react.cljs" (render "src/cljsjs/react.cljs" data)]]
     (concat (templates-by-lib lib data))
     (apply ->files data))))
