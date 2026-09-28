(ns clj-template.logging
  (:require
   [clojure.data.json :as json]
   [taoensso.timbre :as log]))

(defn- write-unknown
  "Fallback for values data.json can't encode, e.g. objects in `ex-data`."
  [x out _]
  (json/write (str x) out))

(defn- json-output-fn
  "Timbre output fn that renders each log event as a single line of JSON."
  [{:keys [instant level ?ns-str ?line msg_ ?err context]}]
  (json/write-str
   (cond-> {:timestamp instant
            :level level
            :logger ?ns-str
            :line ?line
            :message (force msg_)}
     (seq context) (assoc :context context)
     ?err (assoc :error (Throwable->map ?err)))
   :escape-slash false
   :default-write-fn write-unknown))

(def ^:private output-fns
  {:json json-output-fn})

(defn- ->timbre-min-level
  "Combine the default level with per-namespace overrides into timbre's
  `[[ns-pattern level] ...]` form. The first matching pattern wins."
  [min-level ns-min-levels]
  (conj (vec ns-min-levels) ["*" min-level]))

(defn configure!
  "Configure timbre from the `:logging/config` section of config.edn."
  [{:keys [min-level ns-min-levels format]}]
  (log/merge-config! {:min-level (->timbre-min-level min-level ns-min-levels)
                      :output-fn (get output-fns format log/default-output-fn)}))
