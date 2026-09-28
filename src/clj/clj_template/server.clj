(ns clj-template.server
  (:require
   [integrant.core :as ig]
   [ring.adapter.jetty :as jetty]
   [taoensso.timbre :as log])
  (:import
   (org.eclipse.jetty.server
    Server)))

(set! *warn-on-reflection* true)

(defn handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(defmethod ig/init-key ::server
  [_ {:keys [port]}]
  (log/info "Starting server on port" port)
  (jetty/run-jetty handler {:port port
                            :join? false}))

(defmethod ig/halt-key! ::server
  [_ ^Server server]
  (log/info "Stopping server")
  (.stop server))
