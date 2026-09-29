(ns clj-template.logging
  (:require
   [clojure.data.json :as json]
   [taoensso.telemere :as t]))

(defn- write-unknown
  "Fallback for values data.json can't encode, e.g. objects in `ex-data`."
  [x out _]
  (json/write (str x) out))

(defn- write-json
  [x]
  (json/write-str x
                  :escape-slash false
                  :default-write-fn write-unknown))

(def ^:private output-fns
  {:json (t/pr-signal-fn {:pr-fn write-json})
   :text (t/format-signal-fn)})

(defn- ->min-level
  "Combine the default level with per-namespace overrides into telemere's
  `[[ns-pattern level] ...]` form. The first matching pattern wins."
  [min-level ns-min-levels]
  (conj (vec ns-min-levels) ["*" min-level]))

(defn configure!
  "Configure telemere from the `:logging/config` section of config.edn."
  [{:keys [min-level ns-min-levels format]}]
  (t/set-min-level! nil (->min-level min-level ns-min-levels))
  (t/add-handler! :default/console
                  (t/handler:console {:output-fn (get output-fns format (:text output-fns))})
                  {:async nil}))
