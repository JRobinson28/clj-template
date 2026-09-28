(ns clj-template.system
  (:require
   [aero.core :as aero]
   [clj-template.logging :as logging]
   [clojure.java.io :as io]
   [integrant.core :as ig]))

(def ^:private config-path
  "config.edn")

(defn read-config
  "Read the aero config for `profile`, configure logging and return the
  expanded Integrant system config."
  [profile]
  (let [config (aero/read-config (io/resource config-path) {:profile profile})]
    (logging/configure! (:logging/config config))
    (-> (:ig/system config)
        (doto (ig/load-namespaces))
        ig/expand)))
