(ns clojure.core-test.min
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists min
 (deftest test-min
   (are [expected x y] (= expected (min x y) (min y x))
     1      1   2
     1N     1N  2N
     1N     1N  2
     1      1   2N
     1.0    1.0 2.0
     1      1   2.0
     1.0    1.0 2
     1      1   ##Inf
     ##-Inf 1   ##-Inf
     ##-Inf ##-Inf ##Inf
     #?@(:cljs []
         :default
         [1/2    1/2 1]))

   ;; Single arg just returns argument
   (is (= 1 (min 1)))
   (is (= 2 (min 2)))
   (is (= "x" (min "x"))) ; doesn't check single arg for Number

   ;; Multi-arg
   (is (= 1 (min 1 2 3 4 5)))
   (is (= 1 (min 5 4 3 2 1)))
   (is (= ##-Inf (min 1 2 3 4 5 ##-Inf)))
   (is (= 1 (min 1 2 3 4 5 ##Inf)))

   #?@(:cljs
       ;; There are bugs here:
       ;; https://clojure.atlassian.net/browse/CLJS-3425
       [(is (= 1 (min ##NaN 1))) ; Bug
        (is (NaN? (min 1 ##NaN)))
        (is (NaN? (min 1 2 3 4 ##NaN)))
        (is (= ##Inf (min ##-Inf ##NaN ##Inf))) ; Bug
        (is (NaN? (min ##NaN)))

        (is (= "x" (min "x" "y")))
        (is (nil? (min nil 1))) ; nil acts like zero
        (is (nil? (min 1 nil)))]
       :default
       [(is (NaN? (min ##NaN 1)))
        (is (NaN? (min 1 ##NaN)))
        (is (NaN? (min 1 2 3 4 ##NaN)))
        (is (NaN? (min ##-Inf ##NaN ##Inf)))
        (is (NaN? (min ##NaN)))

        (is (thrown? #?(:cljs :default :default Exception) (min "x" "y")))
        (is (thrown? #?(:cljs :default :default Exception) (min nil 1)))
        (is (thrown? #?(:cljs :default :default Exception) (min 1 nil)))])))
