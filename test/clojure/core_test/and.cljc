(ns clojure.core-test.and
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists and
  (deftest test-and
    (testing "common"
      (is (= true (and)))
      (are [x] (= x
                  (and x))
        true
        false
        nil
        :example)
      (are [ex a b] (= ex (and a b))
        true  true  true
        false true  false
        false false true
        false false false
        true  true  true
        nil   true  nil
        nil   nil   true
        nil   nil   nil)
      (testing "binds values before comparing"
        (let [counter (volatile! 0)]
          (is (= nil (and (vswap! counter inc) nil)))
          (is (= 1 @counter))))
      (testing "early exits"
        (let [counter (volatile! 0)]
          (is (= nil (and nil (vswap! counter inc))))
          (is (= 0 @counter))))
      (testing "handles varargs"
        (is (= nil (and nil nil 3)))
        (is (= nil (and nil nil nil nil nil nil nil nil nil nil nil nil true)))))

   (testing "infinite-sequence"
     (is (some?
          (and (range)))))))
