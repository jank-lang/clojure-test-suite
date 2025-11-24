(ns clojure.core-test.gt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists >
  (deftest test->
    (testing "arity 1"
      ;; Doesn't matter what the argument is, `>` return `true` for
      ;; one argument.
      (is (> 1))
      (is (> 0))
      (is (> -1))
      ;; Doesn't check whether arg is a number
      (is (> "abc"))
      (is (> :foo))
      (is (> nil)))

    (testing "arity 2"
      (are [expected x y] (= expected (> x y))
        true 1 0
        true  0 -1
        true  1N 0N
        true  0N -1N
        true  1.0 0.0
        true  0.0 -1.0
        true  1.0M 0.0M
        true  0.0M -1.0M
        true  -1 ##-Inf
        true  ##Inf 1

        false 0 1
        false  -1 0
        false  0N 1N
        false  -1N 0N
        false  0.0 1.0
        false  -1.0 0.0
        false  0.0M 1.0M
        false  -1.0M 0.0M
        false  ##-Inf -1
        false  1 ##Inf

        false 1 ##NaN                   ; Anything compared with ##NaN is false
        false ##NaN 1

        ;; Nothing is greater than itself
        false 0 0
        false 1 1
        false -1 -1
        false ##Inf ##Inf
        false ##-Inf ##-Inf
        false ##NaN ##NaN

        ;; Mixing numeric types should't matter
        true 1.0 0
        true 0 -1.0
        true 1.0M 0N
        true 0.0M -1N)
      
      #?(:cljs nil
         :default
         (testing "Rationals"
           (are [expected x y] (= expected (> x y))
             true 1/2 1/16
             true  0.5 1/16
             true  -1/16 -1/2
             true  -1/16 -0.5

             false 1/16 1/2
             false  -1/2 -1/16
             false  1/16 0.5
             false  -0.5 -1/16))))

    (testing  "arity 3 and more"
      (are [expected x y z] (= expected (> x y z))
        true 2 1 0
        true 0 -1 -2
        true 1 0 -1
        false 2 0 1
        false 0 2 1
        false -1 -2 0
        false -1 0 -2)
      (is (= true (apply > (reverse (range 10)))))
      (is (= false (apply > -1 (reverse (range 10))))))

    (testing "negative tests"
      ;; `>` only compares numbers, except in ClojureScript (really
      ;; JavaScript under the hood) where comparisons are just a bit
      ;; of a mess. CLR also has some implicit conversions for strings
      ;; and characters to numbers.
      #?@(:cljs
          [(is (= true (> 1 nil)))
           (is (= false (> nil 1)))
           (is (= true (> 2 1 nil)))
           (is (= false (> 1 2 nil)))
           (is (= true (> "2" "1")))
           (is (= false (> "bar" "foo")))
           (is (= false (> :bar :foo)))]
          :cljr
          [(is (thrown? Exception (> 1 nil)))
           (is (thrown? Exception (> nil 1)))
           (is (thrown? Exception (> 1 nil 2)))
           (is (thrown? Exception (> 2 1 nil)))
           (is (= true (> "2" "1")))
           (is (thrown? Exception (> "bar" "foo")))
           (is (thrown? Exception (> :bar :foo)))]
          :default
          [(is (thrown? Exception (> 1 nil)))
           (is (thrown? Exception (> nil 1)))
           (is (thrown? Exception (> 1 nil 2)))
           (is (thrown? Exception (> 2 1 nil)))
           (is (thrown? Exception (> "2" "1")))
           (is (thrown? Exception (> "bar" "foo")))
           (is (thrown? Exception (> :bar :foo)))]))))
