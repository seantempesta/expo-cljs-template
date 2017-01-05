(ns {{name}}.core
    (:require [om.next :as om :refer-macros [defui]]
              [re-natal.support :as sup]
              [{{name}}.state :as state]
              [cljs-exponent.components :refer [text view image touchable-highlight] :as rn]))

(def logo-img (js/require "./assets/images/cljs.png"))

(defn alert [title]
  (.alert rn/alert title))

(defui AppRoot
  static om/IQuery
  (query [this]
    '[:app/msg])
  Object
  (render [this]
    (let [{:keys [app/msg]} (om/props this)]
      (view {:style {:flexDirection "column" :margin 40 :alignItems "center"}}
            (text {:style {:fontSize 30 :fontWeight "100" :marginBottom 20 :textAlign "center"}} msg)
            (image {:source logo-img
                    :style  {:width 80 :height 80 :marginBottom 30}})
            (touchable-highlight {:style   {:backgroundColor "#999" :padding 10 :borderRadius 5}
                                  :onPress #(alert "HELLO!")}
                                 (text {:style {:color "white" :textAlign "center" :fontWeight "bold"}} "press me"))))))

(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(defn init []
  (om/add-root! state/reconciler AppRoot 1)
  (.registerComponent rn/app-registry "main" (fn [] app-root)))
