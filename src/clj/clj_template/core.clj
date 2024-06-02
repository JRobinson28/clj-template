
(ns clj-template.core
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [taoensso.timbre :as log])
  (:gen-class))

(def app nil)

(def ^:private config-path
  "config.edn")

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defmethod ig/init-key ::app
  [_ {:keys [logging] :as config}]
  (log/info "Starting app" config)
  (log/set-min-level! (:min-level logging))
  (log/debug "Debug logging enabled")
  config)

(defmethod ig/halt-key! ::app
  [_ _]
  (log/info "Stopping app"))

(defn init-app!
  [env]
  (alter-var-root #'app
                  (fn [_]
                    (-> (io/resource config-path)
                        (aero/read-config {:profile (keyword env)})
                        :ig/system
                        (doto (ig/load-namespaces))
                        ig/init))))

(defn halt-app!
  []
  (when app
    (ig/halt! app)))

(defn -main
  "Application entry point"
  [& [env]]
  (.addShutdownHook (Runtime/getRuntime) (Thread. halt-app!))
  (init-app! env))
