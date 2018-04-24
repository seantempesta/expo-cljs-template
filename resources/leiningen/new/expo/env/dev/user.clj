(ns user
  (:require [figwheel-sidecar.repl-api :as ra]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [hawk.core :as hawk]
            [clojure.tools.reader.edn :as edn]
            [clojure.set :as set]))
;; This namespace is loaded automatically by nREPL


(defn get-cljs-builds
  []
  (let [project-config (->> "project.clj"
                            slurp
                            read-string
                            (drop 1)
                            (apply hash-map))
        profiles (:profiles project-config)]
    (get-in profiles [:dev :cljsbuild :builds])))

(defn enable-source-maps
  []
  (println "Source maps enabled.")
  (let [path "node_modules/metro/src/Server/index.js"]
    (spit path
          (str/replace (slurp path) "/\\.map$/" "/main.map$/"))))

(defn write-main-js
  []
  (-> "'use strict';\n\n// cljsbuild adds a preamble mentioning goog so hack around it\nwindow.goog = {\n  provide() {},\n  require() {},\n};\nrequire('./target/expo/env/index.js');\n"
      ((partial spit "main.js"))))

(defn get-expo-settings []
  (try
    (let [settings (-> (slurp ".expo/settings.json") json/read-str)]
      settings)
    (catch Exception e
      nil)))

(defn get-lan-ip
  "If .lan-ip file exists, it fetches the ip from the file."
  []
  (if-let [ip (try (slurp ".lan-ip") (catch Exception e nil))]
    (clojure.string/trim-newline ip)
    (cond
      (some #{(System/getProperty "os.name")} ["Mac OS X" "Windows 10"])
      (.getHostAddress (java.net.InetAddress/getLocalHost))

      :else
      (->> (java.net.NetworkInterface/getNetworkInterfaces)
           (enumeration-seq)
           (filter #(not (or (str/starts-with? (.getName %) "docker")
                             (str/starts-with? (.getName %) "br-"))))
           (map #(.getInterfaceAddresses %))
           (map
             (fn [ip]
               (seq (filter #(instance?
                               java.net.Inet4Address
                               (.getAddress %))
                            ip))))
           (remove nil?)
           (first)
           (filter #(instance?
                      java.net.Inet4Address
                      (.getAddress %)))
           (first)
           (.getAddress)
           (.getHostAddress)))))

  (defn get-expo-ip []
    (if-let [expo-settings (get-expo-settings)]
      (case (get expo-settings "hostType")
        "lan" (get-lan-ip)
        "localhost" "localhost"
        "tunnel" (throw (Exception. "Expo Setting \"hostType\": \"tunnel\" doesn't work with figwheel. Check .expo/settings.json, please set value to \"lan\" or \"localhost\".")))
      "localhost"))                                         ;; default

  (defn write-env-dev
    "First check the .expo/settings.json file to see what host is specified.  Then set the appropriate IP."
    []
    (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))
          ip (get-expo-ip)]
      (-> "(ns env.dev)\n(def hostname \"%s\")\n(def ip \"%s\")"
          (format
            hostname
            ip)
          ((partial spit "env/dev/env/dev.cljs")))))

  (defn rebuild-env-index
    [js-modules]
    (let [devHost (get-expo-ip)
          modules (->> (file-seq (io/file "assets"))
                       (filter #(and (not (re-find #"DS_Store" (str %)))
                                     (.isFile %)))
                       (map (fn [file] (when-let [unix-path (->> file .toPath .iterator iterator-seq (str/join "/"))]
                                         (str "../../" unix-path))))
                       (concat js-modules ["react" "react-native" "expo" "create-react-class"])
                       (distinct))
          modules-map (zipmap
                        (->> modules
                             (map #(str "\""
                                        (if (str/starts-with? % "../../assets")
                                          (-> %
                                              (str/replace "../../" "./")
                                              (str/replace "\\" "/")
                                              (str/replace "@2x" "")
                                              (str/replace "@3x" ""))
                                          %)
                                        "\"")))
                        (->> modules
                             (map #(format "(js/require \"%s\")"
                                           (-> %
                                               (str/replace "../../" "../../../")
                                               (str/replace "\\" "/")
                                               (str/replace "@2x" "")
                                               (str/replace "@3x" ""))))))]
      (try
        (-> "(ns env.index\n  (:require [env.dev :as dev]))\n\n;; undo main.js goog preamble hack\n(set! js/window.goog js/undefined)\n\n(-> (js/require \"figwheel-bridge\")\n    (.withModules %s)\n    (.start \"main\" \"expo\" \"%s\"))\n"
            (format
              (str "#js " (with-out-str (println modules-map)))
              devHost)
            ((partial spit "env/dev/env/index.cljs")))

        (catch Exception e
          (println "Error: " e)))))

(defn- required-modules
  "returns a vector of string with the names of the imported modules. Ignoring those
  that are commented out"
  [file-content]
  (some->> file-content
           (re-seq #"(?m)^[^;\n]+?\(js/require \"([^\"]+)\"\)")
           (map last)
           (vec)))

  ;; Each file maybe corresponds to multiple modules.
  (defn watch-for-external-modules
    []
    (let [path ".js-modules.edn"]
      (hawk/watch! [{:paths   ["src"]
                     :filter  hawk/file?
                     :handler (fn [ctx {:keys [kind file] :as event}]
                                (let [m (edn/read-string (slurp path))
                                      file-name (-> (.getPath file)
                                                    (str/replace (str (System/getProperty "user.dir") "/") ""))]

                                  ;; file is deleted
                                  (when (= :delete kind)
                                    (let [new-m (dissoc m file-name)]
                                      (spit path new-m)
                                      (rebuild-env-index (flatten (vals new-m)))))

                                  (when (.exists file)
                                    (let [content (slurp file)
                                          js-modules (required-modules content)]
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
                js-modules (required-modules content)]
            (if js-modules
              (swap! m assoc file-name (vec js-modules))))))
      (spit path @m)
      (rebuild-env-index (flatten (vals @m)))))

  (defn init-external-modules
    []
    (rebuild-modules))

  ;; Lein
  (defn start-figwheel
    "Start figwheel for one or more builds"
    [& build-ids]
    (init-external-modules)
    (enable-source-maps)
    (write-main-js)
    (write-env-dev)
    (watch-for-external-modules)
    (ra/start-figwheel!
      {:build-ids        (if (seq build-ids)
                           build-ids
                           ["main"])
       :all-builds       (get-cljs-builds)})
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

  ;; Boot
  (defn prepare
    []
    (init-external-modules)
    (enable-source-maps)
    (write-main-js)
    (write-env-dev)
    (watch-for-external-modules))
