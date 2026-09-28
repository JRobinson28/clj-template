(ns clj-template.server-test
  (:require
    [clj-http.client :as http]
    [clj-template.server :as server]
    [clojure.test :refer [deftest is]]
    [integrant.core :as ig]))

(def ^:private port 2801)

(deftest server-responds
  (let [system (ig/init {::server/server {:port port}})]
    (try
      (let [response (http/get (str "http://localhost:" port))]
        (is (= 200 (:status response)))
        (is (= "Hello World" (:body response))))
      (finally
        (ig/halt! system)))))
