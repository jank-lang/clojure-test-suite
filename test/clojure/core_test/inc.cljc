(ns clojure.core-test.inc
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/inc
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
        6.4    7.4                      ; risky
        #?@(:cljs []
            :default
            [1/2    3/2
             -1/2   1/2])
        ##Inf  ##Inf
        ##-Inf ##-Inf)

      (is (NaN? (inc ##NaN))))

    (testing "overflow"
      #?(:clj (is (thrown? Exception (inc Long/MAX_VALUE)))
	     :cljr (is (thrown? Exception (inc Int64/MaxValue)))
         :cljs (is (= (inc js/Number.MAX_SAFE_INTEGER) (+ 2 js/Number.MAX_SAFE_INTEGER)))
         :default (is false "overflow untested")))

    (testing "inc-nil"
      ;; ClojureScript says (= 1 (inc nil)) because JavaScript casts null to 0
      ;; https://clojuredocs.org/clojure.core/inc#example-6156a59ee4b0b1e3652d754f
      #?(:cljs (is (= 1 (inc #_:clj-kondo/ignore nil)))
         :default (is (thrown? #?(:clj Exception :cljr Exception) (inc #_:clj-kondo/ignore nil)))))))
