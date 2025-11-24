(ns clojure.core-test.lt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists <
  (deftest test-<
    (testing "arity 1"
      ;; Doesn't matter what the argument is, `<` return `true` for
      ;; one argument.
      (is (< 1))
      (is (< 0))
      (is (< -1))
      ;; Doesn't check whether arg is a number
      (is (< "abc"))
      (is (< :foo))
      (is (< nil)))

    (testing "arity 2"
      (are [expected x y] (= expected (< x y))
        true 0 1
        true -1 0
        true 0N 1N
        true -1N 0N
        true 0.0 1.0
        true -1.0 0.0
        true 0.0M 1.0M
        true -1.0M 0.0M
        true ##-Inf -1
        true 1 ##Inf

        false 1 0
        false 0 -1
        false 1N 0N
        false 0N -1N
        false 1.0 0.0
        false 0.0 -1.0
        false 1.0M 0.0M
        false 0.0M -1.0M
        false -1 ##-Inf
        false ##Inf 1
        false 1 ##NaN                   ; Anything compared with ##NaN is false
        false ##NaN 1

        ;; Nothing is less than itself
        false 0 0
        false 1 1
        false -1 -1
        false ##Inf ##Inf
        false ##-Inf ##-Inf
        false ##NaN ##NaN

        ;; Mixing numeric types should't matter
        true 0 1.0
        true -1.0 0
        true 0N 1.0M
        true -1N 0.0M)
      
      #?(:cljs nil
         :default
         (testing "Rationals"
           (are [expected x y] (= expected (< x y))
             true 1/16 1/2
             true -1/2 -1/16
             true 1/16 0.5
             true -0.5 -1/16
             false 1/2 1/16
             false 0.5 1/16
             false -1/16 -1/2
             false -1/16 -0.5))))

    (testing "arity 3 and more"
      (are [expected x y z] (= expected (< x y z))
        true 0 1 2
        true -2 -1 0
        true -1 0 1
        false 1 0 2
        false 1 2 0
        false 0 -2 -1
        false -2 0 -1)
      (is (= true (apply < (range 10))))
      (is (= false (apply < 100 (range 10)))))

    (testing "negative tests"
      ;; `<` only compares numbers, except in ClojureScript (really
      ;; JavaScript under the hood) where comparisons are just a bit
      ;; of a mess.
      #?@(:cljs
          [(is (= true (< nil 1)))
           (is (= false (< 1 nil)))
           (is (= true (< nil 1 2)))
           (is (= false (< 1 2 nil)))
           (is (= true (< "1" "2")))
           (is (= false (< "foo" "bar")))
           (is (= false (< :foo :bar)))]
          :cljr
          [(is (thrown? Exception (< nil 1)))
           (is (thrown? Exception (< 1 nil)))
           (is (thrown? Exception (< nil 1 2)))
           (is (thrown? Exception (< 1 2 nil)))
           (is (= true (< "1" "2")))
           (is (thrown? Exception (< "foo" "bar")))
           (is (thrown? Exception (< :foo :bar)))]
          :default
          [(is (thrown? Exception (< nil 1)))
           (is (thrown? Exception (< 1 nil)))
           (is (thrown? Exception (< nil 1 2)))
           (is (thrown? Exception (< 1 2 nil)))
           (is (thrown? Exception (< "1" "2")))
           (is (thrown? Exception (< "foo" "bar")))
           (is (thrown? Exception (< :foo :bar)))]))))
