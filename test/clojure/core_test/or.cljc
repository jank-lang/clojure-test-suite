(ns clojure.core-test.or
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists or
  (deftest test-or
    (testing "common"
      (is (= nil (or)))
      (are [x] (= x (or x))
        true
        false
        nil
        :example)
      (are [ex a b] (= ex (or a b))
        true  true  true
        true  true  false
        true  false true
        false false false
        true  true  true
        true  true  nil
        true  nil   true
        nil   nil   nil)
      (testing "binds values before comparing"
        (let [counter (volatile! 0)]
          (is (= 1 (or nil (vswap! counter inc))))
          (is (= 1 @counter))))
      (testing "early exits"
        (let [counter (volatile! 0)]
          (is (= true (or true (vswap! counter inc))))
          (is (= 0 @counter))))
      (testing "handles varargs"
        (is (= 3 (or nil nil 3)))
        (is (= true (or nil nil nil nil nil nil nil nil nil nil nil nil true)))))

    (testing "infinite-sequence"
      (is (some? (or (range)))))))
