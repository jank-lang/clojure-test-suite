(ns clojure.core-test.number-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists number?
 (deftest test-number?
   (are [expected x] (= expected (number? x))
     true  0
     true  1
     true  -1
     true  r/max-int
     true  r/min-int
     true  0.0
     true  1.0
     true  -1.0
     true  r/max-double
     true  r/min-double
     true  ##Inf
     true  ##-Inf
     true  ##NaN
     true  0N
     true  1N
     true  -1N
     true  0.0M
     true  1.0M
     true  -1.0M

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

     #?@(:cljs []
         :default
         [true  0/2
          true  1/2
          true  -1/2]))))
