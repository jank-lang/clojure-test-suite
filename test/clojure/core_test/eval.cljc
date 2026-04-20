(ns clojure.core-test.eval
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            #?(:cljs [cljs.js])))                 ; need this for eval support in CLJS

(when-var-exists eval
  (def x 42)

  (deftest test-eval
    #?(:cljs nil
       :default
       (do (testing "Strings, numbers, characters, true, false, nil and keywords evaluate to themselves."
             (are [expected form] (= expected (eval form))
               ;; lots of Clojure objects just evaluate to themselves
               1 1
               0 0
               -1 -1
               1.0 1.0
               1N 1N
               1.0M 1.0M
               1/2 1/2
               "a string" "a string"
               "(+ 1 2)" "(+ 1 2)" ; strings are just evaluated as strings
               \x \x
               true true
               false false
               nil nil
               :a-keyword :a-keyword))

           (testing "Functions"
             (is (fn? (eval (fn [x] x))))
             (is (fn? (eval '(fn [x] x))))
             (is (fn? (eval +)))
             (is (fn? (eval '+))))

           (testing "Symbol resolution"
             ;; namespace qualified
             (is (= 42 (eval 'clojure.core-test.eval/x))))

           (testing "Vectors, Maps, Sets"
             ;; basic literal collections
             (is (= [:a :b] (eval [:a :b])))
             (is (= {:a :b} (eval {:a :b})))
             (is (= #{:a :b} (eval #{:a :b})))
             ;; collections with embedded symbols
             (is (= [:a :b 42] (eval [:a :b 'clojure.core-test.eval/x]))))

           (testing "Lists, function application, and macros"
             ;; empty list evaluates to itself
             (is (= '() (eval '())))
             ;; function calls
             (is (= 5 (eval '(+ 2 3))))
             (is (= 6 (eval '(* 2 3))))
             ;; macros
             (is (= 42 (eval '(or false clojure.core-test.eval/x))))
             (is (= 42 (eval '(and (+ 2 3) clojure.core-test.eval/x))))
             ;; special forms
             (is (= 43 (eval '(let [y 43] (or false y)))))
             (is (= 43 (eval '(loop [y 0] (if (= y 43) y (recur (inc y))))))))))))
