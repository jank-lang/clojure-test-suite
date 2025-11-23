(ns clojure.core-test.dec
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

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
      #?(:clj (is (thrown? Exception (dec Long/MIN_VALUE)))
         :cljr (is (thrown? Exception (dec Int64/MinValue)))
         :cljs (is (= (dec js/Number.MIN_SAFE_INTEGER) (- js/Number.MIN_SAFE_INTEGER 2)))
         :default (is false "TODO underflow")))

    (testing "dec-nil"
      ;; ClojureScript says (= -1 (dec nil)) because JavaScript casts null to 0
      #?(:cljs (is (= -1 (dec nil)))
         :default (is (thrown? Exception (dec nil)))))))
