(ns clojure.core-test.zero-qmark
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists zero?
  (deftest test-zero?
    (are [expected x] (= expected (zero? x))
      true  0
      true  0.0
      true  0.0M
      true  0N

      false 1
      false -1
      false r/min-int
      false r/max-int
      false 1.0
      false -1.0
      false r/min-double
      false r/max-double
      false ##Inf
      false ##-Inf
      false ##NaN
      false 1N
      false -1N
      false 1.0M
      false -1.0M

      #?@(:cljs []
          :default
          [true  0/2
           false 1/2
           false -1/2]))

    (is #?@(:cljs [(= false (zero? nil))]
            :default [(thrown? Exception (zero? nil))]))
    (is #?@(:cljs [(= false (zero? false))]
            :default [(thrown? Exception (zero? false))]))
    (is #?@(:cljs [(= false (zero? true))]
            :default [(thrown? Exception (zero? true))]))))
