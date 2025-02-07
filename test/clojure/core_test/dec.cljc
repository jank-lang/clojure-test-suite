(ns clojure.core-test.dec
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/dec
  (deftest test-dec
    (testing "common"
      (are [in ex] (= (dec in) ex)
        1      0
        0      -1
        1N     0N
        0N     -1N
        14412  14411
        -3     -4
        7.4    6.4                      ; risky
        #?@(:cljs []
            :default
            [3/2    1/2
             1/2    -1/2])
        ##Inf  ##Inf
        ##-Inf ##-Inf)

      (is (NaN? (dec ##NaN))))

    (testing "underflow"
      #?(:clj (is (thrown? Exception (dec Long/MIN_VALUE)))
         :cljs (is (= (dec js/Number.MIN_SAFE_INTEGER) (- js/Number.MIN_SAFE_INTEGER 2)))
         :default (is false "TODO underflow")))

    (testing "dec-nil"
      ;; ClojureScript says (= -1 (dec nil)) because JavaScript casts null to 0
      #?(:clj (is (thrown? Exception (dec #_:clj-kondo/ignore nil)))
         :cljs (is (= -1 (dec #_:clj-kondo/ignore nil)))))))
