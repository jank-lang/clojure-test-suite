(ns clojure.core-test.plus-squote
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/+'
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

    (is (thrown? #?(:cljs :default :clj Exception) (+' 1 nil)))
    (is (thrown? #?(:cljs :default :clj Exception) (+' nil 1)))

    (is (instance? clojure.lang.BigInt (+' 0 1N)))
    (is (instance? clojure.lang.BigInt (+' 0N 1)))
    (is (instance? clojure.lang.BigInt (+' 0N 1N)))
    (is (instance? clojure.lang.BigInt (+' 1N 1)))
    (is (instance? clojure.lang.BigInt (+' 1 1N)))
    (is (instance? clojure.lang.BigInt (+' 1N 1N)))
    (is (instance? clojure.lang.BigInt (+' 1 5N)))
    (is (instance? clojure.lang.BigInt (+' 1N 5)))
    (is (instance? clojure.lang.BigInt (+' 1N 5N)))

    (is (instance? clojure.lang.BigInt (+' -1 r/min-int)))
    (is (instance? clojure.lang.BigInt (+' r/min-int -1)))))
