(ns ^:figwheel-no-load env.main
  (:require [om.next :as om]
            [{{name}}.core :as core]
            [{{name}}.state :as state]
            [figwheel.client :as figwheel :include-macros true]
            [env.dev]))

(enable-console-print!)

(figwheel/watch-and-reload
 :websocket-url (str "ws://" env.dev/ip ":3449/figwheel-ws")
 :heads-up-display false
 :jsload-callback #(om/add-root! state/reconciler core/AppRoot 1))

(core/init)

(def root-el (core/app-root))
