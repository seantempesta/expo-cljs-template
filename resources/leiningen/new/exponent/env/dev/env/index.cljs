(ns index)

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js{:react-native (js/require "react-native")
                      :react (js/require "react")})
    (.start "main"))
