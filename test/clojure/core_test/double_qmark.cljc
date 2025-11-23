(ns clojure.core-test.double-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists double?
  (deftest test-double?
    (are [expected x] (= expected (double? x))
         #?@(:cljs
             [true 0
              true 1
              true -1
              true r/max-int
              true r/min-int]
             :default
             [false 0
              false 1
              false -1
              false r/max-int
              false r/min-int])
         true  0.0
         true  1.0
         true  -1.0
         #?@(:jank
             [;; Since currently jank doesn't support `float` (`jank::f32`) literal values only `double` (`jank::f64`)
              ;; literal values, there is no difference in the implementation of `float` and `double`.
              true (float 0.0)
              true (float 1.0)
              true (float -1.0)]
             :cljs
             [true (float 0.0)
              true (float 1.0)
              true (float -1.0)]
             :default
             [false (float 0.0) ; surprising since (float? (double 0.0)) = true
              false (float 1.0) ; surprising since (float? (double 1.0)) = true
              false (float -1.0) ; surprising since (float? (double -1.0)) = true
              ])
         true  (double 0.0)
         true  (double 1.0)
         true  (double -1.0)
         true  r/max-double
         true  r/min-double
         true  ##Inf
         true  ##-Inf
         true  ##NaN
         #?@(:cljs
             [true 0N
              true 1N
              true -1N
              true 0.0M
              true 1.0M
              true -1.0M]
             :default
             [false 0N
              false 1N
              false -1N
              false 0.0M
              false 1.0M
              false -1.0M])
      #?@(:cljs [] ; CLJS doesn't have ratios
          :default
          [false 0/2
           false 1/2
           false -1/2])
      false nil
      false true
      false false
      false "a string"
      false "0"
      false "1"
      false "-1"
      false {:a :map}
      false #{:a-set}
      false [:a :vector]
      false '(:a :list)
      false \0
      false \1
      false :a-keyword
      false :0
      false :1
      false :-1
      false 'a-sym)))
