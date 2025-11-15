(ns clojure.core-test.string-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists string?
  (deftest test-string?
    (are [expected x] (= expected (string? x))
      true "a string"
      true "0"
      true "1"
      true "-1"

      false 0
      false 1
      false -1
      false r/max-int
      false r/min-int
      false 0.0
      false 1.0
      false -1.0
      false (float 0.0)
      false (float 1.0)
      false (float -1.0)
      false (double 0.0)
      false (double 1.0)
      false (double -1.0)
      false r/max-double
      false r/min-double
      false ##Inf
      false ##-Inf
      false ##NaN
      false 0N
      false 1N
      false -1N
      false 0.0M
      false 1.0M
      false -1.0M
      false nil
      false true
      false false
      false {:a :map}
      false #{:a-set}
      false [:a :vector]
      false '(:a :list)
      false :a-keyword
      false :0
      false :1
      false :-1
      false 'a-sym

      #?@(:cljs
          [true \0
           true \1
           true \A
           true \space]
          :default
          [false \0
           false \1
           false \A
           false \space])
      #?@(:cljs []
          :default
          [false 0/2
           false 1/2
           false -1/2]))))
