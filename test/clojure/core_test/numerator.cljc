(ns clojure.core-test.numerator
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists numerator
 (deftest test-numerator
   #?@(:cljs []
       :default
       [(is (= 1 (numerator 1/2)))
        (is (= 2 (numerator 2/3)))
        (is (= 3 (numerator 3/4)))])

   (is (thrown? #?(:cljs :default :default Exception) (numerator 1)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator 1.0)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator 1N)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator 1.0M)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator ##Inf)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator ##NaN)))
   (is (thrown? #?(:cljs :default :default Exception) (numerator nil)))))
