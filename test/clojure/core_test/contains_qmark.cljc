(ns clojure.core-test.contains-qmark
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists contains?
  (deftest test-contains?
    (testing "common"
      (is (= false (contains? nil nil)))
      (is (= false (contains? {} nil)))
      (is (= false (contains? [] nil)))
      #?(:cljs (is (= false (contains? "abc" "a")))
         :default (is (thrown? Exception (contains? "abc" "a"))))

      ;; find by index
      (is (= true (contains? ["a" "b" "c"] 0)))
      (is (= false (contains? ["a" "b" "c"] 3)))
      (is (= true (contains? "abc" 0)))
      (is (= true (contains? "abc" 2)))
      (is (= false (contains? "abc" 3)))

      ;; find by key
      (is (= true (contains? {:a 1 :b 1} :a)))
      (is (= false (contains? {:a 1 :b 1} :c)))
      (is (= true (contains? {:a 1 :b (range)} :a))))))
