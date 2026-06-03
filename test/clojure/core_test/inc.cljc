(ns clojure.core-test.inc
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.number-range :refer [max-int]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists inc
  (deftest test-inc
    (testing "common"
      (are [in ex] (= (inc in) ex)
        0      1
        1      2
        -1     0
        0N     1N
        -1N    0N
        14411  14412
        -4     -3
        6.4    7.4
        ##Inf  ##Inf
        ##-Inf ##-Inf
        #?@(:cljs []
            :default
            [1/2    3/2
             -1/2   1/2]))

      (is (NaN? (inc ##NaN))))

    (testing "overflow"
      #?(;; Phel integers avoid overflow by being promoted to BigInteger
         :phel (is (not (= (inc max-int) (+ 2 max-int))))
         :lpy nil  ; Python integers cannot overflow
         :cljs (is (= (inc max-int) (+ 2 max-int)))
         :default (is (p/thrown? (inc max-int)))))

    (testing "inc-nil"
      ;; ClojureScript says (= 1 (inc nil)) because JavaScript casts null to 0
      ;; https://clojuredocs.org/clojure.core/inc#example-6156a59ee4b0b1e3652d754f
      #?(:cljs (is (= 1 (inc nil)))
         :default (is (p/thrown? (inc nil)))))))
