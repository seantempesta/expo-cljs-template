(ns {{name}}.core
    (:require [om.next :as om :refer-macros [defui]]
              [re-natal.support :as sup]
              [{{name}}.state :as state]))

(def logo-img (js/require "./assets/images/cljs.png"))

(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))

(defn create-element [rn-comp opts & children]
      (apply js/React.createElement rn-comp (clj->js opts) children))

(def expo (js/require "expo"))
(def app-registry (.-AppRegistry ReactNative))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def image (partial create-element (.-Image ReactNative)))
(def touchable-highlight (partial create-element (.-TouchableHighlight ReactNative)))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

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
  (.registerRootComponent expo app-root))
