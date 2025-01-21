(ns clojure.core-test.neg-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/neg?
 (deftest test-neg?
   (are [expected x] (= expected (neg? x))
     false 0
     false 1
     true  -1
     true  r/min-int
     false r/max-int
     false 0.0
     false 1.0
     true  -1.0
     false r/min-double
     false r/max-double
     false ##Inf
     true  ##-Inf
     false ##NaN
     false 0N
     false 1N
     true  -1N
     #?@(:cljs []
         :default
         [false 0/2
          false 1/2
          true  -1/2])
     false 0.0M
     false 1.0M
     true  -1.0M)

   (is (thrown? #?(:cljs :default :clj Exception) (neg? nil)))
   (is (thrown? #?(:cljs :default :clj Exception) (neg? false)))
   (is (thrown? #?(:cljs :default :clj Exception) (neg? true)))))
