(ns clojure.core-test.char-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists char?
  (deftest test-char?
    (are [expected x] (= expected (char? x))
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
      #?@(:cljs [] ; cljs doesn't have ratios
          :default
          [false 0/2
           false 1/2
           false -1/2])
      false 0.0M
      false 1.0M
      false -1.0M
      false nil
      false true
      false false
      false "a string"
      #?@(:cljs ; In cljs, chars are single element strings
          [true "0"
           true "1"]
          :default
          [false "0"
           false "1"])
      false "-1"
      false {:a :map}
      false #{:a-set}
      false [:a :vector]
      false '(:a :list)
      true  \0
      true  \1
      true  \A
      true  \space
      false :a-keyword
      false :0
      false :1
      false :-1
      false 'a-sym))
  )
