(ns clj-template.logging-test
  (:require
   [clj-template.logging :as logging]
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [taoensso.timbre :as log]))

(defn- log-json
  "Run `f` with JSON logging and return the parsed log events. Errors go to
  stderr, so capture both streams."
  [f]
  (let [out (with-out-str
              (binding [*err* *out*]
                (log/with-merged-config {:min-level :info
                                         :output-fn #'logging/json-output-fn}
                  (f))))]
    (map #(json/read-str % :key-fn keyword) (str/split-lines out))))

(deftest json-output
  (testing "events are single-line JSON"
    (let [[event] (log-json #(log/info "hello" {:a 1}))]
      (is (= "info" (:level event)))
      (is (= "hello {:a 1}" (:message event)))
      (is (= "clj-template.logging-test" (:logger event)))
      (is (string? (:timestamp event)))
      (is (nil? (:error event)))))

  (testing "context is included when set"
    (let [[event] (log-json #(log/with-context {:request-id "abc"}
                               (log/info "hello")))]
      (is (= {:request-id "abc"} (:context event)))))

  (testing "errors are structured, including non-JSON ex-data"
    (let [[event] (log-json #(log/error (ex-info "boom" {:obj (Object.)}) "failed"))]
      (is (= "failed" (:message event)))
      (is (= "boom" (get-in event [:error :cause])))
      (is (string? (get-in event [:error :data :obj]))))))

(deftest ns-min-levels
  (log/with-merged-config {:min-level (#'logging/->timbre-min-level
                                       :info
                                       {"org.eclipse.jetty.*" :warn})}
    (testing "overridden namespaces use their own level"
      (is (not (log/may-log? :info "org.eclipse.jetty.server.Server")))
      (is (log/may-log? :warn "org.eclipse.jetty.server.Server")))
    (testing "other namespaces use the default level"
      (is (log/may-log? :info "clj-template.server"))
      (is (not (log/may-log? :debug "clj-template.server"))))))
