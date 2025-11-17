(ns clojure.core-test.keys
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists keys
  (deftest test-keys
    (testing "common"
      (is (= nil (keys nil)))
      (is (= nil (keys {})))
      (is (= nil (keys [])))
      (is (= nil (keys '())))
      (is (= nil (keys #{})))
      (is (= nil (keys "")))
      (is (= '(0) (keys {0 0.0})))
      (is (= '(:a) (keys {:a :b})))
      (is (= '(:a) (keys {:a (range)})))
      (is (= '("a") (keys {"a" :b})))
      (is (= '([:a :b]) (keys {[:a :b] :c})))
      (is (= '((:a)) (keys {(keys {:a :b}) :c})))
      #?@(:cljs [(is (thrown? js/Error (keys 0)))]
          :default [(is (thrown? Exception (keys 0)))]))))
