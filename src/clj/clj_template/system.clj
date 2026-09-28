(ns clj-template.system
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [taoensso.timbre :as log]))

(def ^:private config-path
  "config.edn")

(defn read-config
  "Read the aero config for `profile`, configure logging and return the
  expanded Integrant system config."
  [profile]
  (let [config (aero/read-config (io/resource config-path) {:profile profile})]
    (log/set-min-level! (get-in config [:logging/config :min-level]))
    (-> (:ig/system config)
        (doto (ig/load-namespaces))
        ig/expand)))
