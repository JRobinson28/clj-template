(ns basic-test
  (:require [clojure.test :refer [deftest is testing]]))

(deftest basic-test
  (testing "Basic test"
    (is (= 2 (+ 1 1)))))

