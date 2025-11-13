(ns clojure.core-test.bigint
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists bigint
  (deftest test-bigint
    (are [expected x] (= expected (bigint x))
      1N  1
      0N  0
      -1N -1
      #?@(:cljs []
          :cljr [179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368N r/max-double]
		      :default [179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000N r/max-double])
      1N  1.0
      0N  0.0
      -1N -1.0
      #?@(:cljs []
          :default
          [1N  12/12
           0N  0/12
           -1N -12/12]))

    ;; Validate that we correctly promote from int to bigint with `inc'` and `dec'`.
    (is (= 9223372036854775808N (inc (bigint r/max-int)) (inc' r/max-int)))
    (is (= -9223372036854775809N (dec (bigint r/min-int)) (dec' r/min-int)))

    #?@(:cljs []
        :jank []
        :default
        [(is (instance? clojure.lang.BigInt (bigint 0)))
         (is (instance? clojure.lang.BigInt (bigint 0.0)))
         (is (instance? clojure.lang.BigInt (inc' r/max-int)))
         (is (instance? clojure.lang.BigInt (dec' r/min-int)))])))
