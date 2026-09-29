(ns clj-template.logging-test
  (:require
   [clj-template.logging :as logging]
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing]]
   [taoensso.telemere :as t]))

(defn- log-json
  "Run `f`, trapping its signals, and return the last one rendered by the JSON
  output fn, parsed."
  [f]
  (let [signal (t/with-signal true (f))
        output-fn (:json @#'logging/output-fns)]
    (json/read-str (output-fn signal) :key-fn keyword)))

(deftest json-output
  (testing "events are single-line JSON"
    (let [event (log-json #(t/log! {:level :info :data {:a 1}} "hello"))]
      (is (= "info" (:level event)))
      (is (= "hello" (:msg_ event)))
      (is (= {:a 1} (:data event)))
      (is (= "clj-template.logging-test" (:ns event)))
      (is (string? (:inst event)))
      (is (nil? (:error event)))))

  (testing "context is included when set"
    (let [event (log-json #(t/with-ctx {:request-id "abc"}
                             (t/log! "hello")))]
      (is (= {:request-id "abc"} (:ctx event)))))

  (testing "errors are structured, including non-JSON ex-data"
    (let [event (log-json #(t/log! {:level :error
                                    :error (ex-info "boom" {:obj (Object.)})}
                                   "failed"))]
      (is (= "failed" (:msg_ event)))
      (is (= "boom" (get-in event [:error 0 :msg])))
      (is (string? (get-in event [:error 0 :data :obj]))))))

(deftest ns-min-levels
  (t/with-min-level nil (#'logging/->min-level :info {"org.eclipse.jetty.*" :warn})
    (do
      (testing "overridden namespaces use their own level"
        (is (not (t/signal-allowed? {:level :info :ns "org.eclipse.jetty.server.Server"})))
        (is (t/signal-allowed? {:level :warn :ns "org.eclipse.jetty.server.Server"})))
      (testing "other namespaces use the default level"
        (is (t/signal-allowed? {:level :info :ns "clj-template.server"}))
        (is (not (t/signal-allowed? {:level :debug :ns "clj-template.server"})))))))
