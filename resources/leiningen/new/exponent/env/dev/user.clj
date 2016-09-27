(ns user
  (:require [figwheel-sidecar.repl-api :as ra]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def profiles (->> "project.clj"
                   slurp
                   read-string
                   (drop-while #(not= % :profiles))
                   (apply hash-map)
                   :profiles))

(def cljs-builds (get-in profiles [:dev :cljsbuild :builds]))

(defn write-dev-env
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))
        ip (.getHostAddress (java.net.InetAddress/getLocalHost))]
    (-> "(ns env.dev)\n(def hostname \"%s\")\n(def ip \"%s\")"
        (format hostname ip)
        ((partial spit "env/dev/env/dev.cljs")))))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [& build-ids]
  (write-dev-env)

  (ra/start-figwheel!
   {:build-ids  (if (seq build-ids )
                  build-ids
                  ["main"])
    :all-builds cljs-builds})
  (ra/cljs-repl))

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(start-figwheel)
