(ns clojure.core-test.dec
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.number-range :refer [min-int]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists dec
  (deftest test-dec
    (testing "common"
      (are [in ex] (= (dec in) ex)
        1      0
        0      -1
        1N     0N
        0N     -1N
        14412  14411
        -3     -4
        7.4    6.4
        ##Inf  ##Inf
        ##-Inf ##-Inf
        #?@(:cljs []
            :default
            [3/2    1/2
             1/2    -1/2]))

      (is (NaN? (dec ##NaN))))

    (testing "underflow"
      #?(:phel    (is (= (dec min-int) (dec min-int)))
         :lpy     []  ; Python integers cannot underflow
         :cljs    (is (= (dec min-int) (- min-int 2)))
         :default (is (p/thrown? (dec min-int)))))

    (testing "dec-nil"
      ;; ClojureScript says (= -1 (dec nil)) because JavaScript casts null to 0
      #?(:cljs    (is (= -1 (dec nil)))
         :default (is (p/thrown? (dec nil)))))))
