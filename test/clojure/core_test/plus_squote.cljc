(ns clojure.core-test.plus-squote
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists big-int?] :as p]))

(when-var-exists +'
  (deftest test-+'
    (testing "arity-0"
      ;; always returns zero
      (is (zero? (+'))))

    (testing "arity-1"
      ;; returns the argument
      (are [x] (= x (+' x))
        1
        0
        -1
        1N
        0N
        -1N
        1.0
        0.0
        -1.0
        1.0M
        0.0M
        -1.0M
        #?@(:cljs []
            :default
            [1/2
             1/3])
        r/max-int
        r/min-int
        r/max-double
        r/min-double))

    (testing "arity-2"
      (are [sum addend summand] (= sum (+' addend summand))
        ;; long + long
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

        ;; double + double
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

        ;; double + long
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

        ;; long + double
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

        ;; bigdecimal + long
        0.0M 0.0M 0
        1.0M 1.0M 0
        1.0M 0.0M 1
        2.0M 1.0M 1
        6.0M 1.0M 5
        6.0M 5.0M 1
        10.0M 5.0M 5
        0.0M 1.0M -1
        0.0M -1.0M 1
        -2.0M -1.0M -1
        -1.0M -1.0M 0
        -1.0M 0.0M -1

        ;; long + bigdecimal
        0.0M 0 0.0M
        1.0M 1 0.0M
        1.0M 0 1.0M
        2.0M 1 1.0M
        6.0M 1 5.0M
        6.0M 5 1.0M
        10.0M 5 5.0M
        0.0M 1 -1.0M
        0.0M -1 1.0M
        -2.0M -1 -1.0M
        -1.0M -1 0.0M
        -1.0M 0 -1.0M

        ;; ints and bigints
        1N 0 1N
        1N 0N 1
        1N 0N 1N
        2N 1N 1
        2N 1 1N
        2N 1N 1N
        6N 1 5N
        6N 1N 5
        6N 1N 5N

        ;; check infinities, same as `+`
        ##Inf 1 ##Inf
        ##Inf ##Inf 1
        ##-Inf -1 ##-Inf
        ##-Inf ##-Inf -1)

      ;; NaN contaminates everything
      (is (NaN? (+' 1N ##NaN)))
      (is (NaN? (+' ##NaN 1N)))
      (is (NaN? (+' ##NaN ##NaN)))

      (is (NaN? (+' ##Inf ##-Inf)))

      (is (big-int? (+' 0 1N)))
      (is (big-int? (+' 0N 1)))
      (is (big-int? (+' 0N 1N)))
      (is (big-int? (+' 1N 1)))
      (is (big-int? (+' 1 1N)))
      (is (big-int? (+' 1N 1N)))
      (is (big-int? (+' 1 5N)))
      (is (big-int? (+' 1N 5)))
      (is (big-int? (+' 1N 5N)))

      ;; Check that integer -> big integer promotions occur
      (is (big-int? (+' r/max-int 1)))
      (is (big-int? (+' 1 r/max-int)))
      (is (big-int? (+' -1 r/min-int)))
      (is (big-int? (+' r/min-int -1))))

    (testing "arity-3+"
      (is (= 10 (+' 1 2 3 4)))
      (is (= 15N (+' 1N 2N 3N 4N 5N)))
      (is (= 15N (+' 1 2 3 4 5N)))
      (is (= 15N (+' 1N 2 3 4 5)))
      (is (= 15N (+' 1 2 3N 4 5)))
      (is (= 15.0 (+' 1.0 2 3 4 5N)))   ; doubles promote to doubles
      ;; Ideally, this would compare against a contant expected value,
      ;; but we don't know what r/max-int is on every implementation,
      ;; so we'll use `*'` as a bit of a hack.
      (is (= (*' 4 r/max-int)
             (+' r/max-int r/max-int r/max-int r/max-int))))

    (testing "exception cases"
      (is (p/thrown? (+' 1 nil)))
      (is (p/thrown? (+' nil 1))))))
