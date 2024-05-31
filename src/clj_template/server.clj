(ns clj-template.server
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as ring]
            [taoensso.timbre :as log]))

(defn- handler
  [_]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(defmethod ig/init-key ::server
  [_ {:keys [port] :as _server}]
  (log/info "Starting server on port" port)
  (ring/run-jetty handler {:port port}))

(defmethod ig/halt-key! ::server
  [_ server]
  (log/info "Stopping server")
  (.stop server))
