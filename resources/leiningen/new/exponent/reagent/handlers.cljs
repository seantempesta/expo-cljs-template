(ns {{name}}.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [clojure.spec :as s]
    [{{name}}.db :as db :refer [app-db]]))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check failed: " explain-data) explain-data)))))

(def validate-spec-mw
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(register-handler
  :initialize-db
  validate-spec-mw
  (fn [_ _]
    app-db))

(register-handler
  :set-greeting
  validate-spec-mw
  (fn [db [_ value]]
    (assoc db :greeting value)))
