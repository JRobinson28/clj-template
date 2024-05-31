(ns user
  (:require [hawk.core :as hawk]
            [integrant.repl :as igr]
            [nrepl.cmdline :as nrepl]))

(defonce ^:dynamic stop nil)

(defn shutdown!
  [{:keys [file-watcher]}]
  (when file-watcher
    (hawk/stop! file-watcher))
  (igr/halt))

(defn go []
  (let [file-watcher (hawk/watch! [{:paths ["src/clj" "src/cljc"]}
                                   :filter hawk/file?
                                   :handler (fn [_ _]
                                              (binding [*ns* *ns*]
                                                (igr/reset)))])]
    (igr/go)
    (alter-var-root #'stop (fn [_]
                             #(shutdown! {:file-watcher file-watcher})))))

(defn go-with-nrepl
  [_]
  (require '[nrepl.cmdline :as nrepl])
  (go)
  (nrepl/-main))

(comment

  ;; start system
  (go)

  ;;reload system
  (igr/reset)

  ;;stop system
  (when stop
    (stop))
  )
