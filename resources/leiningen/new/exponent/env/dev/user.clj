(ns user
  (:require [figwheel-sidecar.repl-api :as ra]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hawk.core :as hawk]
            [clojure.tools.reader.edn :as edn]
            [clojure.set :as set]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def project-config (->> "project.clj"
                         slurp
                         read-string
                         (drop 1)
                         (apply hash-map)))

(def profiles (:profiles project-config))

(def cljs-builds (get-in profiles [:dev :cljsbuild :builds]))

(defn enable-source-maps
  []
  (println "Enabled source maps.")
  (let [path "node_modules/react-native/packager/react-packager/src/Server/index.js"]
    (spit path
          (str/replace (slurp path) "/\\.map$/" "/main.map$/"))))

(defn write-main-js
  []
  (-> "'use strict';\n\n// cljsbuild adds a preamble mentioning goog so hack around it\nwindow.goog = {\n  provide() {},\n  require() {},\n};\nrequire('./target/env/index.js');\n"
      ((partial spit "main.js"))))

(defn get-lan-ip
  []
  (cond
    (= "Mac OS X" (System/getProperty "os.name"))
    (.getHostAddress (java.net.InetAddress/getLocalHost))

    :else
    (->> (java.net.NetworkInterface/getNetworkInterfaces)
         (enumeration-seq)
         (filter #(and (not (.isLoopback %))
                       (not (str/starts-with? (.getName %) "docker"))))
         (map #(.getInterfaceAddresses %))
         (first)
         (filter #(instance?
                   java.net.Inet4Address
                   (.getAddress %)))
         (first)
         (.getAddress)
         (.getHostAddress))))

(defn write-env-dev
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))
        ip (get-lan-ip)]
    (-> "(ns env.dev)\n(def hostname \"%s\")\n(def ip \"%s\")"
        (format
         hostname
         ip)
        ((partial spit "env/dev/env/dev.cljs")))))

(defn keyword->str [s]
  (str/replace s
               #"::(\S)*"
               (fn [r]
                 (str \" (str/replace (first r) #"::" "") \"))))

(defn rebuild-env-index
  [js-modules]
  (let [modules (->> (file-seq (io/file "assets"))
                     (filter #(and (not (re-find #"DS_Store" (str %)))
                                   (.isFile %)))
                     (map (fn [file] (when-let [path (str file)]
                                      (str "../../" path))))
                     (concat js-modules ["react" "react-native" "exponent"])
                     (distinct))
        modules-map (zipmap
                     (->> modules
                          (map #(str "::"
                                     (if (str/starts-with? % "../../assets")
                                       (-> %
                                           (str/replace "../../" "./")
                                           (str/replace "@2x" "")
                                           (str/replace "@3x" ""))
                                       %))))
                     (->> modules
                          (map #(format "(js/require \"%s\")"
                                        (-> %
                                            (str/replace "@2x" "")
                                            (str/replace "@3x" ""))))))]
    (try
      (-> "(ns env.index\n  (:require [env.dev :as dev]))\n\n;; undo main.js goog preamble hack\n(set! js/window.goog js/undefined)\n\n(-> (js/require \"figwheel-bridge\")\n    (.withModules %s)\n    (.start \"main\"))\n"
          (format
           (str "#js " (with-out-str (println modules-map))))
          (keyword->str)
          ((partial spit "env/dev/env/index.cljs")))

      (catch Exception e
        (println "Error: " e)))))

;; Each file maybe corresponds to multiple modules.
(defn watch-for-external-modules
  []
  (let [path ".js-modules.edn"]
    (hawk/watch! [{:paths ["src"]
                   :filter hawk/file?
                   :handler (fn [ctx {:keys [kind file] :as event}]
                              (let [m (edn/read-string (slurp path))
                                    file-name   (-> (.getPath file)
                                                    (str/replace (str (System/getProperty "user.dir") "/") ""))]

                                ;; file is deleted
                                (when (= :delete kind)
                                  (let [new-m (dissoc m file-name)]
                                    (spit path new-m)
                                    (rebuild-env-index (flatten (vals new-m)))))

                                (when (.exists file)
                                  (let [content (slurp file)
                                        js-modules (some->>
                                                    content
                                                    (re-seq #"\(js/require \"([^\"]+)\"\)")
                                                    (map last)
                                                    (vec))
                                        commented-modules (some->>
                                                           content
                                                           (re-seq #"[;]+[\s]*\(js/require \"([^\"]+)\"\)")
                                                           (map last)
                                                           (set))
                                        js-modules (if commented-modules
                                                     (vec (remove commented-modules js-modules))
                                                     js-modules)]
                                    (let [old-js-modules (get m file-name)]
                                      (when (not= old-js-modules js-modules)
                                        (let [new-m (if (seq js-modules)
                                                      (assoc m file-name js-modules)
                                                      (dissoc m file-name))]
                                          (spit path new-m)

                                          (rebuild-env-index (flatten (vals new-m)))))))))
                              ctx)}])))

(defn rebuild-modules
  []
  (let [path ".js-modules.edn"
        m (atom {})]
    ;; delete path
    (when (.exists (java.io.File. path))
      (clojure.java.io/delete-file path))

    (doseq [file (file-seq (java.io.File. "src"))]
      (when (.isFile file)
        (let [file-name (-> (.getPath file)
                            (str/replace (str (System/getProperty "user.dir") "/") ""))
              content (slurp file)
              js-modules (some->>
                          content
                          (re-seq #"\(js/require \"([^\"]+)\"\)")
                          (map last)
                          (vec))
              commented-modules (some->>
                                 content
                                 (re-seq #"[;]+[\s]*\(js/require \"([^\"]+)\"\)")
                                 (map last)
                                 (set))
              js-modules (if commented-modules
                           (vec (remove commented-modules js-modules))
                           js-modules)]
          (if js-modules
            (swap! m assoc file-name (vec js-modules))))))
    (spit path @m)
    (rebuild-env-index (flatten (vals @m)))))

(defn init-external-modules
  []
  (rebuild-modules))

(defn start-figwheel
  "Start figwheel for one or more builds"
  [& build-ids]
  (init-external-modules)
  (enable-source-maps)
  (write-main-js)
  (write-env-dev)
  (watch-for-external-modules)
  (ra/start-figwheel!
   {:figwheel-options {}
    :build-ids  (if (seq build-ids)
                  build-ids
                  ["main"])
    :all-builds cljs-builds})
  (ra/cljs-repl))

(defn stop-figwheel
  "Stops figwheel"
  []
  (ra/stop-figwheel!))

(defn -main
  [args]
  (case args
    "--figwheel"
    (start-figwheel)

    "--rebuild-modules"
    (rebuild-modules)

    (prn "You can run lein figwheel or lein rebuild-modules.")))
