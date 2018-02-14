(ns re-natal.support
  "Helpers and adapters to be able to mount/remount Rum components in a React Native application.")

(def React (js/require "react"))
(def create-factory (.-createFactory React))
(def create-class (js/require "create-react-class"))

(defonce root-component (atom nil))
(defonce mounted-element (atom nil))

(defn make-root-component-factory
  "Returns a React Native component factory fn for the root componenet singleton"
  []
  (create-factory
    (create-class
      #js {:getInitialState (fn []
                              (this-as this
                                (if-not @root-component
                                  (reset! root-component this)
                                  (throw (js/Error. "ASSERTION FAILED: re-natal.support root component mounted more than once.")))))
           :render          (fn [] @mounted-element)})))

(defn mount
  "A modified version of rum.core/mount to work with React Native and re-natal.
  Since React Native's root component is a singleton, mount doesn't apply in the context of a DOM element (like in React),
  but applies globally to the app. This function mounts/replaces the current "
  [element]
  (reset! mounted-element element)
  (when @root-component
    (.forceUpdate @root-component)))
