(ns user
  (:require
   [clj-template.system :as system]
   [integrant.repl :as igr :refer [go halt reset]]
   [nrepl.cmdline :as nrepl]))

(igr/set-prep! #(system/read-config :dev))

(defn go-with-nrepl
  [_]
  (go)
  (nrepl/-main))

(comment

  ;; start system
  (go)

  ;; reload changed namespaces and restart system
  (reset)

  ;; stop system
  (halt))
