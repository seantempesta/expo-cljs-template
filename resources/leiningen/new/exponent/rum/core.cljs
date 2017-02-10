(ns {{name}}.core
    (:require-macros [rum.core :refer [defc]])
    (:require [re-natal.support :as support]
              [rum.core :as rum]
              [cljs-exponent.components :refer [text view image touchable-highlight] :as rn]))

(def logo-img (js/require "./assets/images/cljs.png"))

(defn alert [title]
  (.alert rn/alert title))

(defonce app-state (atom {:greeting "Hello Clojure in iOS and Android!"}))

(defc AppRoot < rum/reactive [state]
  (view {:style {:flexDirection "column" :margin 40 :alignItems "center"}}
        (text {:style {:fontSize 30 :fontWeight "100" :marginBottom 20 :textAlign "center"}} (:greeting (rum/react state)))
        (image {:source logo-img
                :style  {:width 80 :height 80 :marginBottom 30}})
        (touchable-highlight {:style   {:backgroundColor "#999" :padding 10 :borderRadius 5}
                              :onPress #(alert "HELLO!")}
                             (text {:style {:color "white" :textAlign "center" :fontWeight "bold"}} "press me"))))

(defonce root-component-factory (support/make-root-component-factory))

(defn mount-app [] (support/mount (AppRoot app-state)))

(defn init []
  (mount-app)
  (.registerComponent rn/app-registry "main" (fn [] root-component-factory)))
