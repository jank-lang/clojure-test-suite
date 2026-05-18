(ns clojure.core-test.denominator
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists denominator
  (deftest test-denominator
    (is (= 2 (denominator 1/2)))
    (is (= 3 (denominator 2/3)))
    (is (= 4 (denominator 3/4)))

    #?@(:lpy
        [(is (= 1 (denominator 1)))
         (is (= 1 (denominator 1N)))]
        :default
        [(is (p/thrown? (denominator 1)))
         (is (p/thrown? (denominator 1N)))])
    (are [x] (p/thrown? (denominator x))
      1.0
      1.0M
      ##Inf
      ##NaN
      nil)))
