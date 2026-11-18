(ns clojure.core-test.if-not
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/if-not

  ;; DOCSTRING:
  ;; clojure.core/if-not
  ;; [test then]
  ;; [test then else]
  ;; Evaluates test. If logical false, evaluates and returns then expr, 
  ;; otherwise else expr, if supplied, else nil.

  (deftest test-if-not
    (testing "a falsey test evaluates and returns `then`"
      (is (= :then (if-not nil :then :else)))
      (is (= :then (if-not false :then :else)))
      (is (= :then (if-not (= 1 2) :then :else))))

    (testing "a truthy test evaluates and returns `else`"
      (is (= :else (if-not true :then :else)))
      (is (= :else (if-not (= 1 1) :then :else)))
      ;; only nil and false are falsey
      (is (= :else (if-not 0 :then :else)))
      (is (= :else (if-not "" :then :else))))

    (testing "with no `else`, a truthy test returns nil"
      (is (nil? (if-not true :then)))
      (is (= :then (if-not nil :then))))

    (testing "only the selected expression is evaluated"
      (is (= :then (if-not nil :then (throw (ex-info "unreachable" {})))))
      (is (= :else (if-not true (throw (ex-info "unreachable" {})) :else)))
      (let [calls (volatile! 0)]
        (is (= 1 (if-not false (vswap! calls inc) :else)))
        (is (= 1 @calls))))))
