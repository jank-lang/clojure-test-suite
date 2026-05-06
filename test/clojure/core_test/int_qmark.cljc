(ns clojure.core-test.int-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists int?
  (deftest test-int?
    (are [expected x] (= expected (int? x))
      true  0
      true  1
      true  -1
      true  r/max-int
      true  r/min-int
      false 0.1
      false 1.1
      false -1.1
      false r/max-double
      false r/min-double
      false ##Inf
      false ##-Inf
      false ##NaN
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
      false 'a-sym
      #?@(:cljs
          [true 0.0
           true 1.0
           true -1.0
           true 0N
           true 1N
           true -1N
           true 0.0M
           true 1.0M
           true -1.0M]
          :lpy
          [false 0.0
           false 1.0
           false -1.0
           true 0N
           true 1N
           true -1N
           false 0.0M
           false 1.0M
           false -1.0M
           true 0/2
           false 1/2
           false -1/2]
          :phel
          [false 0.0
           false 1.0
           false -1.0
           true 0N
           true 1N
           true -1N
           false 0.0M
           false 1.0M
           false -1.0M
           true 0/2
           false 1/2
           false -1/2]
          :default
          [false 0.0
           false 1.0
           false -1.0
           false 0N
           false 1N
           false -1N
           false 0.0M
           false 1.0M
           false -1.0M
           true 0/2
           false 1/2
           false -1/2]))))
