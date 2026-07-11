(ns clojure.core-test.star-squote
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists big-int?] :as p]))

(when-var-exists *'
  (deftest test-*'
    (testing "arity-0"
      (is (= 1 (*'))))

    (testing "arity-1"
      (are [expected x] (= expected (*' x))
        1 1
        2 2
        10 10
        1000 1000
        10000000000000000000000000N 10000000000000000000000000N
        1.0 1.0
        1.0M 1.0M))
    
    (testing "arity-2"
      (are [prod x y] (= prod (*' x y))
        ;; ints
        0 0 0
        0 1 0
        0 0 1
        1 1 1
        5 1 5
        5 5 1
        25 5 5
        -1 1 -1
        -1 -1 1
        1 -1 -1
        0 -1 0
        0 0 -1
        (inc r/min-int) r/max-int -1
        (inc r/min-int) -1 r/max-int

        ;; doubles
        0.0 0.0 0.0
        0.0 1.0 0.0
        0.0 0.0 1.0
        1.0 1.0 1.0
        5.0 1.0 5.0
        5.0 5.0 1.0
        25.0 5.0 5.0
        -1.0 1.0 -1.0
        -1.0 -1.0 1.0
        1.0 -1.0 -1.0
        0.0 -1.0 0.0
        0.0 0.0 -1.0

        ;; big decimals
        0.0M 0.0M 0.0M
        0.0M 1.0M 0.0M
        0.0M 0.0M 1.0M
        1.0M 1.0M 1.0M
        5.0M 1.0M 5.0M
        5.0M 5.0M 1.0M
        25.0M 5.0M 5.0M
        -1.0M 1.0M -1.0M
        -1.0M -1.0M 1.0M
        1.0M -1.0M -1.0M
        0.0M -1.0M 0.0M
        0.0M 0.0M -1.0M

        ;; mixed doubles and ints
        0.0 0.0 0
        0.0 1.0 0
        0.0 0.0 1
        1.0 1.0 1
        5.0 1.0 5
        5.0 5.0 1
        25.0 5.0 5
        -1.0 1.0 -1
        -1.0 -1.0 1
        1.0 -1.0 -1
        0.0 -1.0 0
        0.0 0.0 -1

        ;; mixed ints and doubles (swap order)
        0.0 0 0.0
        0.0 1 0.0
        0.0 0 1.0
        1.0 1 1.0
        5.0 1 5.0
        5.0 5 1.0
        25.0 5 5.0
        -1.0 1 -1.0
        -1.0 -1 1.0
        1.0 -1 -1.0
        0.0 -1 0.0
        0.0 0 -1.0

        ;; mixed ints and big integers
        0 0 1N
        0 0N 1
        0 0N 1N
        1 1N 1
        1 1 1N
        1 1N 1N
        5 1 5N
        5 1N 5
        5 1N 5N

        ;; mixed ints and big decimals
        0.0M 0 0.0M
        0.0M 1 0.0M
        0.0M 0 1.0M
        1.0M 1 1.0M
        5.0M 1 5.0M
        5.0M 5 1.0M
        25.0M 5 5.0M
        -1.0M 1 -1.0M
        -1.0M -1 1.0M
        1.0M -1 -1.0M
        0.0M -1 0.0M
        0.0M 0 -1.0M

        ;; mixed big decimals and ints
        0.0M 0.0M 0
        0.0M 1.0M 0
        0.0M 0.0M 1
        1.0M 1.0M 1
        5.0M 1.0M 5
        5.0M 5.0M 1
        25.0M 5.0M 5
        -1.0M 1.0M -1
        -1.0M -1.0M 1
        1.0M -1.0M -1
        0.0M -1.0M 0
        0.0M 0.0M -1

        ;; mixed big ints and big decimals
        0.0M 0N 0.0M
        0.0M 1N 0.0M
        0.0M 0N 1.0M
        1.0M 1N 1.0M
        5.0M 1N 5.0M
        5.0M 5N 1.0M
        25.0M 5N 5.0M
        -1.0M 1N -1.0M
        -1.0M -1N 1.0M
        1.0M -1N -1.0M
        0.0M -1N 0.0M
        0.0M 0N -1.0M

        ;; mixed big decimals and big ints
        0.0M 0.0M 0N
        0.0M 1.0M 0N
        0.0M 0.0M 1N
        1.0M 1.0M 1N
        5.0M 1.0M 5N
        5.0M 5.0M 1N
        25.0M 5.0M 5N
        -1.0M 1.0M -1N
        -1.0M -1.0M 1N
        1.0M -1.0M -1N
        0.0M -1.0M 0N
        0.0M 0.0M -1N

        ;; Infinities
        ##Inf 2 ##Inf
        ##Inf ##Inf 2
        ##-Inf -2 ##Inf
        ##-Inf ##Inf -2
        ##-Inf 2 ##-Inf
        ##-Inf ##-Inf 2
        ##Inf -2 ##-Inf
        ##Inf ##-Inf -2
        ##Inf ##Inf ##Inf
        ##-Inf ##Inf ##-Inf
        ##-Inf ##-Inf ##Inf
        ##Inf ##-Inf ##-Inf)

      ;; Check for cases that result in ##NaN
      (are [x y] (NaN? (*' x y))
        ##NaN 1
        1 ##NaN
        ##NaN -1
        -1 ##NaN
        0 ##Inf
        ##Inf 0
        0 ##-Inf
        ##-Inf 0)

      (is (big-int? (*' 0 1N)))
      (is (big-int? (*' 0N 1)))
      (is (big-int? (*' 0N 1N)))
      (is (big-int? (*' 1N 1)))
      (is (big-int? (*' 1 1N)))
      (is (big-int? (*' 1N 1N)))
      (is (big-int? (*' 1 5N)))
      (is (big-int? (*' 1N 5)))
      (is (big-int? (*' 1N 5N)))

      (is (big-int? (*' -1 r/min-int)))
      (is (big-int? (*' r/min-int -1)))
      (is (big-int? (*' (long (/ r/min-int 2)) 3)))
      (is (big-int? (*' 3 (long (/ r/min-int 2))))))

    (testing "arity-3+"
      (is (= 120 (*' 1 2 3 4 5)))
      (is (= 620448401733239439360000N (apply *' (range 1 25)))))

    (testing "exception cases"
      (is (p/thrown? (*' 1 nil)))
      (is (p/thrown? (*' nil 1)))
      (is (p/thrown? (*' nil 1 2)))
      (is (p/thrown? (*' 1 2 nil))))))
