(ns clj-template.core
  (:gen-class)
  (:require
    [clj-template.system :as system]
    [integrant.core :as ig]
    [taoensso.timbre :as log]))


(defonce app nil)


(defmethod ig/init-key ::app
  [_ config]
  (log/info "Starting app" config)
  (log/debug "Debug logging enabled")
  config)


(defmethod ig/halt-key! ::app
  [_ _]
  (log/info "Stopping app"))


(defn init-app!
  [profile]
  (alter-var-root #'app
                  (fn [_]
                    (ig/init (system/read-config profile)))))


(defn halt-app!
  []
  (when app
    (ig/halt! app)))


(defn -main
  "Application entry point"
  [& [env]]
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable halt-app!))
  (init-app! (keyword (or env "prod"))))
