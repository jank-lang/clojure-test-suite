(ns clojure.core-test.neg-qmark
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists neg?
  (deftest test-neg?
    (are [expected x] (= expected (neg? x))
      true  -1.0
      true  -1
      true  r/min-int
      true  ##-Inf
      true  -1N
      true  -1.0M

      false 0
      false 1
      false r/max-int
      false 0.0
      false 1.0
      false r/min-double
      false r/max-double
      false ##Inf
      false ##NaN
      false 0N
      false 1N
      false 0.0M
      false 1.0M
      #?@(:cljs []
          :default
          [false 0/2
           false 1/2
           true  -1/2]))

    #?@(:cljs
        [(is (not (neg? nil)))
         (is (not (neg? false))) ; Prints warning
         (is (not (neg? true)))] ; Prints warning
        :default
        [(is (thrown? Exception (neg? nil)))
         (is (thrown? Exception (neg? false)))
         (is (thrown? Exception (neg? true)))])))
