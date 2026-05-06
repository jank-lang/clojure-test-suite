(ns clojure.core-test.inc
  (:require [clojure.test :as t :refer [are deftest is testing]]
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
      #?(:clj (is (p/thrown? (inc Long/MAX_VALUE)))
         :cljr (is (p/thrown? (inc Int64/MaxValue)))
         :cljs (is (= (inc js/Number.MAX_SAFE_INTEGER) (+ 2 js/Number.MAX_SAFE_INTEGER)))
         ;; Phel integers avoid overflow by being promoted to BigInteger
         :phel nil (is (not (= (inc php/PHP_INT_MAX) (+ 2 php/PHP_INT_MAX))))
         :lpy nil  ; Python integers cannot overflow
         :default (is false "overflow untested")))

    (testing "inc-nil"
      ;; ClojureScript says (= 1 (inc nil)) because JavaScript casts null to 0
      ;; https://clojuredocs.org/clojure.core/inc#example-6156a59ee4b0b1e3652d754f
      #?(:cljs (is (= 1 (inc nil)))
         :default (is (p/thrown? (inc nil)))))))
