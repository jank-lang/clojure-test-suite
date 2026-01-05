(ns clojure.core-test.plus-squote
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists +'
  (deftest test-+'
    (are [sum addend summand] (= sum (+' addend summand))
      0 0 0
      1 1 0
      1 0 1
      2 1 1
      6 1 5
      6 5 1
      10 5 5
      0 1 -1
      0 -1 1
      -2 -1 -1
      -1 -1 0
      -1 0 -1
      (+ 1N r/max-int) r/max-int 1
      (- r/min-int 1N) -1 r/min-int

      0.0 0.0 0.0
      1.0 1.0 0.0
      1.0 0.0 1.0
      2.0 1.0 1.0
      6.0 1.0 5.0
      6.0 5.0 1.0
      10.0 5.0 5.0
      0.0 1.0 -1.0
      0.0 -1.0 1.0
      -2.0 -1.0 -1.0
      -1.0 -1.0 0.0
      -1.0 0.0 -1.0

      0.0 0.0 0
      1.0 1.0 0
      1.0 0.0 1
      2.0 1.0 1
      6.0 1.0 5
      6.0 5.0 1
      10.0 5.0 5
      0.0 1.0 -1
      0.0 -1.0 1
      -2.0 -1.0 -1
      -1.0 -1.0 0
      -1.0 0.0 -1

      0.0 0 0.0
      1.0 1 0.0
      1.0 0 1.0
      2.0 1 1.0
      6.0 1 5.0
      6.0 5 1.0
      10.0 5 5.0
      0.0 1 -1.0
      0.0 -1 1.0
      -2.0 -1 -1.0
      -1.0 -1 0.0
      -1.0 0 -1.0

      1N 0 1N
      1N 0N 1
      1N 0N 1N
      2N 1N 1
      2N 1 1N
      2N 1N 1N
      6N 1 5N
      6N 1N 5
      6N 1N 5N)

    #?(:jank []
       :default [(is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (+' 1 nil)))
                 (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (+' nil 1)))])

    (is (p/big-int? (+' 0 1N)))
    (is (p/big-int? (+' 0N 1)))
    (is (p/big-int? (+' 0N 1N)))
    (is (p/big-int? (+' 1N 1)))
    (is (p/big-int? (+' 1 1N)))
    (is (p/big-int? (+' 1N 1N)))
    (is (p/big-int? (+' 1 5N)))
    (is (p/big-int? (+' 1N 5)))
    (is (p/big-int? (+' 1N 5N)))

    (is (p/big-int? (+' -1 r/min-int)))
    (is (p/big-int? (+' r/min-int -1)))))
