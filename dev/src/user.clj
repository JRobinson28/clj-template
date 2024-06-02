(ns user
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [hawk.core :as hawk]
            [integrant.core :as ig]
            [integrant.repl :as igr]
            [nrepl.cmdline :as nrepl]))

(defonce ^:dynamic stop nil)

(defn- new-system
  []
  (-> (io/resource "config.edn")
      (aero/read-config)
      :ig/system
      (doto (ig/load-namespaces))
      ig/expand))

(defn shutdown!
  [{:keys [file-watcher]}]
  (when file-watcher
    (hawk/stop! file-watcher))
  (igr/halt))

(defn go []
  (let [file-watcher (hawk/watch! [{:paths ["src/clj" "src/cljc"]
                                    :filter hawk/file?
                                    :handler (fn [_ _]
                                               (binding [*ns* *ns*]
                                                 (igr/reset)))}])]
    (igr/set-prep! new-system)
    (igr/go)
    (alter-var-root #'stop
                    (fn [_]
                      #(shutdown! {:file-watcher file-watcher})))))

(defn go-with-nrepl
  [_]
  (require '[nrepl.cmdline :as nrepl])
  (go)
  (nrepl/-main))

(comment

  ;; start system
  (go)

  ;; reload system
  (igr/reset)

  ;; stop system
  (when stop
    (stop)))
