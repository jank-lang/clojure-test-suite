(ns clojure.core-test.comp
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/comp

  ;; DOCSTRING:
  ;; clojure.core/comp
  ;; []
  ;; [f]
  ;; [f g]
  ;; [f g & fs]
  ;; Takes a set of functions and returns a fn that is the composition
  ;; of those fns.  The returned fn takes a variable number of args,
  ;; applies the rightmost of fns to the args, the next
  ;; fn (right-to-left) to the result, etc.

  (deftest test-comp
    (testing "'returns a fn' for each documented arity"
      (is (ifn? (comp)))
      (is (ifn? (comp inc)))
      (is (ifn? (comp inc inc)))
      (is (ifn? (comp inc inc inc inc inc))))

    (testing "'composition of those fns' - no fns is the identity fn"
      (is (= 5 ((comp) 5)))
      (is (= :keyword ((comp) :keyword)))
      (is (= [1 2] ((comp) [1 2])))
      (is (nil? ((comp) nil))))

    (testing "a single fn behaves like that fn"
      (is (= 2 ((comp inc) 1)))
      (is (= "42" ((comp str) 42))))

    (testing "'applies ... right-to-left'"
      ;; `inc` and doubling don't commute, so the result shows the direction:
      ;; 3 -> 6 -> 7 right-to-left, not 3 -> 4 -> 8 left-to-right.
      (is (= 7 ((comp inc #(* % 2)) 3)))
      (is (= 8 ((comp #(* % 2) inc) 3)))
      ;; likewise, each fn appends its own letter: "" -> "c" -> "b" -> "a".
      (is (= "cba" ((comp #(str % "a")
                          #(str % "b")
                          #(str % "c"))
                    "")))
      (is (= "<[x]>" ((comp #(str "<" % ">")
                            #(str "[" % "]"))
                      "x"))))

    (testing "'applies the rightmost of fns to the args' - only it sees all args"
      ;; `+` receives 1, 2 and 3; `str` receives the single result 6.
      (is (= "6" ((comp str +) 1 2 3)))
      (is (= "10" ((comp str +) 1 2 3 4)))
      ;; fns to the left of the rightmost receive exactly one arg each.
      (is (= '(6) ((comp (fn [& args] args) +) 1 2 3)))
      (is (= 7 ((comp inc +) 1 2 3))))

    (testing "[f g & fs] chains and large arg lists"
      (is (= 5 ((comp inc inc inc inc inc) 0)))
      (is (= "3" ((comp str inc inc) 1)))
      (is (= "4950" (apply (comp str +) (range 100))))
      (is (= 4951 (apply (comp inc +) (range 100)))))

    (testing "nested composition associates"
      (is (= 3 ((comp inc (comp inc inc)) 0)))
      (is (= 3 ((comp (comp inc inc) inc) 0))))

    (testing "works with lazy, infinite sequences without realizing them"
      (is (= 1 ((comp inc first) (range))))
      (is (= 3 ((comp inc first rest rest) (range))))
      (is (= "0" ((comp str first) (range)))))

    (testing "component fns still enforce their arity"
      ;; CLJS doesn't enforce arity on fn invocation at all: `(inc)` is
      ;; NaN rather than an arity error, and extra args are ignored.
      #?(:cljs nil
         :default (do (is (p/thrown? ((comp inc inc))))
                      (is (p/thrown? ((comp inc inc) 1 2))))))))