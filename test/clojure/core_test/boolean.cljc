(ns clojure.core-test.boolean
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists boolean
 (deftest test-boolean
   (are [expected x] (= expected (boolean x))
     true  0
     true  1
     true  -1
     true  r/max-int
     true  r/min-int
     true  0.0
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
     true  0N
     true  1N
     true  -1N
     true  0.0M
     true  1.0M
     true  -1.0M
     false nil
     true  true
     false false
     true  "a string"
     true  "0"
     true  "1"
     true  "-1"
     true  "true"
     true  "false"
     true  {:a :map}
     true  #{:a-set}
     true  [:a :vector]
     true  '(:a :list)
     true  \0
     true  \1
     true  :a-keyword
     true  :true
     true  :false
     true  :0
     true  :1
     true  :-1
     true  'a-sym

     #?@(:cljs []
         :default
         [true  0/2
          true  1/2
          true  -1/2]))))
