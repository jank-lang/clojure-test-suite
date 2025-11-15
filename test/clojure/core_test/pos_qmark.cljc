(ns clojure.core-test.pos-qmark
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists pos?
  (deftest test-pos?
    (are [expected x] (= expected (pos? x))
      false 0
      true  1
      false -1
      false r/min-int
      true  r/max-int
      false 0.0
      true  1.0
      false -1.0
      true  r/min-double
      true  r/max-double
      true  ##Inf
      false ##-Inf
      false ##NaN
      false 0N
      true  1N
      false -1N
      false 0.0M
      true  1.0M
      false -1.0M

      #?@(:cljs []
          :default
          [false 0/2
           true  1/2
           false -1/2]))

    #?@(:cljs
        [(is (not (pos? nil)))
         (is (not (pos? false))) ; Prints warning
         (is (pos? true))] ; Prints warning
        :default
        [(is (thrown? Exception (pos? nil)))
         (is (thrown? Exception (pos? false)))
         (is (thrown? Exception (pos? true)))])))
