(ns clojure.core-test.eq-eq
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/==

  ;; DOCSTRING:
  ;; clojure.core/==
  ;; [x]
  ;; [x y]
  ;; [x y & more]
  ;; Returns non-nil if nums all have the equivalent
  ;; value (type-independent), otherwise false

  (deftest test-==
    (testing "arity 1"
      ;; With a single argument there is nothing to compare, so `==`
      ;; returns `true` without even checking that the argument is a
      ;; number.
      (are [x] (true? (== x))
        0
        1
        -1
        1N
        1.0
        1M
        ##Inf
        ##-Inf
        ##NaN
        nil
        :foo
        "abc"
        [1 2]))

    (testing "arity 2, same numeric type"
      (are [expected x y] (= expected (== x y))
        true 0 0
        true 42 42
        true -1 -1
        true 1N 1N
        true 1M 1M
        true 1.0 1.0
        true 1.5M 1.5M
        true ##Inf ##Inf
        true ##-Inf ##-Inf

        false 1 2
        false 0 -1
        false 1N 0N
        false 1M 2M
        false 1.0 0.0
        false -1.5M 1.5M))

    (testing "arity 2, type-independent value comparison"
      (are [expected x y] (= expected (== x y))
        true 1 1.0
        true 1 1N
        true 1 1M
        true 1N 1.0M
        true 100N 100.0
        ;; Integers beyond the precision of a double still compare
        ;; equal to the double they round to.
        true 9223372036854775807 9.223372036854776E18

        false 1 2.0
        false 1M 2
        false 1N 1.5
        false 0 0.5
        ;; Two values that differ in the last place of a double are
        ;; not equivalent.
        false 1 1.0000000000000002))

    (testing "arity 2, zero and negative zero"
      ;; `-0.0` and `0.0` have the same numeric value
      (are [expected x y] (= expected (== x y))
        true 0.0 0.0
        true 0.0 -0.0
        true -0.0 0.0
        true 0 -0.0
        true 0 -0.0M
        true -0.0 0M))

    (testing "arity 2, ##NaN"
      ;; `##NaN` is not equal to anything, not even itself.
      (are [expected x y] (= expected (== x y))
        false ##NaN ##NaN
        false ##NaN 0
        false 0 ##NaN
        false ##NaN ##-Inf
        false ##NaN 1.0M
        false 1.0M ##NaN))

    (testing "arity 2, ##Inf"
      (are [expected x y] (= expected (== x y))
        true ##Inf ##Inf
        true ##-Inf ##-Inf
        false ##Inf ##-Inf
        false ##-Inf ##Inf
        ;; no finite value is infinite
        false ##Inf 1e308
        false ##-Inf 0))

    #?(:cljs nil
       :default
       (testing "arity 2, ratios"
         (are [expected x y] (= expected (== x y))
           ;; Ratios are compared by value
           true 1/2 1/2
           true 2/4 1/2
           true -1/2 -1/2
           ;; ... and that value is type-independent, too
           true 1/2 0.5
           true 3/2 1.5
           true 1/3 0.3333333333333333
           true 1/2 0.5M
           true 0.5M 1/2
           true 2 4/2

           false 1/2 0.51
           false 22/7 3.14
           false 1/2 1M
           false 1/2 1N
           false 2 1/2
           false -1/2 1/2
           false ##NaN 2/3)))

    (testing "arity 2, single-precision and double-precision floats"
      ;; A float and a double with the same value are equal, but the
      ;; double nearest to 0.1 is not the same value as the float
      ;; nearest to 0.1. (ClojureScript has only doubles, so the two
      ;; forms are the same number there.)
      (is (== (float 0.5) (double 0.5)))
      #?(:cljs (is (== (float 0.1) (double 0.1)))
         :default (is (not (== (float 0.1) (double 0.1))))))

    (testing "arity 3 and more"
      (are [expected x y z] (= expected (== x y z))
        true 0 0 0
        true 1 1 1
        true -1 -1 -1
        true 1.5 1.5 1.5
        true ##Inf ##Inf ##Inf
        true ##-Inf ##-Inf ##-Inf
        ;; type-independent
        true 1 1.0 1M
        true 2.0 2 2N
        true 0 0.0 -0.0

        ;; every argument must be equivalent, not just the neighbors
        false 1 1 2
        false 1 2 1
        false 2 1 1
        false -1 1 -1
        false 0 0 0.5
        false ##Inf ##Inf ##-Inf
        false 1 1.0 1.0000000000000002
        ;; ##NaN anywhere makes the result false
        false ##NaN 0 0
        false 0 ##NaN 0
        false 0 0 ##NaN)

      (is (true? (== 1 1.0 1N 1M)))
      (is (false? (== 1 1.0 1N 1M 2)))
      (is (true? (apply == 1 (repeat 5 1))))
      (is (false? (apply == 1 (repeat 5 2))))
      (is (false? (apply == (concat (repeat 4 1) [2]))))
      #?(:cljs nil
         :default (is (true? (== 1/2 0.5 0.5M)))))

    (testing "non-numeric arguments"
      ;; `==` is defined on numbers. Clojure JVM, Babashka and
      ;; ClojureCLR throw when handed a non-number. ClojureScript
      ;; compiles `==` down to a JavaScript comparison and silently
      ;; compares the values instead.
      #?@(:cljs
          [(is (== nil nil))
           (is (not (== nil 1)))
           (is (not (== 1 nil)))
           (is (== "abc" "abc"))
           (is (not (== "1" 1)))
           ;; the same holds in the variadic arities
           (is (not (== 1 1 nil)))
           (is (not (== nil 1 1)))]

          :default
          [(are [x] (p/thrown? (== x x))
             true
             :foo
             "abc"
             [1])

           (is (p/thrown? (== nil 1)))
           (is (p/thrown? (== 1 nil)))
           (is (p/thrown? (== 1 1 nil)))
           (is (p/thrown? (== 1 nil 1)))]))))
