(ns clojure.core-test.pos-int-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists pos-int?
  (deftest test-pos-int?
    (are [expected x] (= expected (pos-int? x))
      false 0
      true  1
      false -1
      false 0.0
      false -1.0
      false r/max-double
      false r/min-double
      false ##Inf
      false ##-Inf
      false ##NaN
      false 0N
      false -1N
      false 0.0M
      false -1.0M
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

      ;; Python VMs integer types are arbitrary precision and have no min or max.
      #?@(:lpy []
          :default
          [true  r/max-int
           false r/min-int])

      #?@(:cljs [true 1.0
                 true 1.0M]
          :default [false 1.0
                    #?(:lpy true :default false) 1N
                    false 0/2
                    false 1/2
                    false -1/2
                    false 1.0M]))))
