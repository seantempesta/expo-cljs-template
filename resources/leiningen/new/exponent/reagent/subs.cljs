(ns {{name}}.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-greeting
 (fn [db _]
   (:greeting db)))
