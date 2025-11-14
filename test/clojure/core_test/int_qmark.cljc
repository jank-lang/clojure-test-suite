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
     #?@(:cljs [true] :default [false]) 0.0
     #?@(:cljs [true] :default [false]) 1.0
     #?@(:cljs [true] :default [false]) -1.0
     false 0.1
     false 1.1
     false -1.1
     false r/max-double
     false r/min-double
     false ##Inf
     false ##-Inf
     false ##NaN
     #?@(:cljs [true] :default [false]) 0N
     #?@(:cljs [true] :default [false]) 1N
     #?@(:cljs [true] :default [false]) -1N
     #?@(:cljs []
         :default
         [true  0/2
          false 1/2
          false -1/2])
     #?@(:cljs [true] :default [false]) 0.0M
     #?@(:cljs [true] :default [false]) 1.0M
     #?@(:cljs [true] :default [false]) -1.0M
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
