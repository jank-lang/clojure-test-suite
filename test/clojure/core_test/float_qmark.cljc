(ns clojure.core-test.float-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists float?
 (deftest test-float?
   (are [expected x] (= expected (float? x))
     #?@(:cljs ; In CLJS all numbers satisfy `float?`
         [true 0
          true 1
          true -1
          true r/max-int
          true r/min-int
          true 0.0
          true 1.0
          true -1.0
          true (float 0.0)
          true (float 1.0)
          true (float -1.0)
          true (double 0.0)
          true (double 1.0)
          true (double -1.0)
          true r/max-double
          true r/min-double
          true ##Inf
          true ##-Inf
          true ##NaN
          true 0N
          true 1N
          true -1N
          true 0.0M
          true 1.0M
          true -1.0M]
         :default
         [true  0.0
          true  1.0
          true  -1.0
          true  (float 0.0)
          true  (float 1.0)
          true  (float -1.0)
          true  (double 0.0)
          true  (double 1.0)
          true  (double -1.0)
          true  r/max-double
          true  r/min-double
          true  ##Inf
          true  ##-Inf
          true  ##NaN

          false 0
          false 1
          false -1
          false r/max-int
          false r/min-int
          false 0N
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
