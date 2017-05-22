(ns ^:figwheel-no-load env.main
  (:require [{{name}}.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [env.dev]))

(enable-console-print!)

(figwheel/watch-and-reload
 :websocket-url (str "ws://" env.dev/ip ":3449/figwheel-ws")
 :heads-up-display false
 ;; TODO make this Rum something
 :jsload-callback #(#'core/mount-app))

(core/init)

(def root-el (core/root-component-factory))
