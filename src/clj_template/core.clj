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
  [_ config]
  (log/info "Starting app" config)
  config)

(defmethod ig/halt-key! ::app
  [_ _]
  (log/info "Stopping app"))

(defn init-app! []
  (alter-var-root #'app
                  (fn [_]
                    (-> (io/resource config-path)
                        (aero/read-config)
                        :ig/system
                        (doto (ig/load-namespaces))
                        ig/init))))

(defn halt-app!
  []
  (when app
    (ig/halt! app)))

(defn -main
  "Application entry point"
  [& _args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. halt-app!))
  (init-app!))
