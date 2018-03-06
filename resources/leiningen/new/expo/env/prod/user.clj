(ns user
  (:require [cljs.build.api :as api]))

(defn get-prod-build
  []
  (let [project-config (->> "project.clj"
                            slurp
                            read-string
                            (drop 1)
                            (apply hash-map))
        profiles (:profiles project-config)]
    (get-in profiles [:prod :cljsbuild :builds 0])))

(defn -main
  [& args]
  (let [{:keys [source-paths compiler]} (get-prod-build)]
    (api/build source-paths compiler)))
