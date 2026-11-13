(ns clojure.core-test.cond
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/cond

  ;; DOCSTRING:
  ;; clojure.core/cond
  ;; [& clauses]
  ;; Takes a set of test/expr pairs. It evaluates each test one at a
  ;; time.  If a test returns logical true, cond evaluates and returns
  ;; the value of the corresponding expr and doesn't evaluate any of the
  ;; other tests or exprs. (cond) returns nil.

  (deftest test-cond
    (testing "no clauses"
      (is (nil? (cond))))

    (testing "single clause"
      (is (= :a (cond true :a)))
      (is (nil? (cond false :a)))
      (is (nil? (cond nil :a)))
      ;; the expr of a true test is returned as-is, even when it's false or nil
      (is (false? (cond true false)))
      (is (nil? (cond true nil)))
      ;; a false or nil expr does not cause a later clause to be selected
      (is (nil? (cond true nil false :a)))
      (is (false? (cond true false :else :a))))

    (testing "logical truth"
      (testing "false and nil are logically false"
        (is (nil? (cond false :matched)))
        (is (nil? (cond nil :matched))))
      (testing "every other value is logically true"
        (is (= :matched (cond true :matched true :not-matched)))
        (is (= :matched (cond 0 :matched true :not-matched)))
        (is (= :matched (cond 1 :matched true :not-matched)))
        (is (= :matched (cond -1 :matched true :not-matched)))
        (is (= :matched (cond 0.0 :matched true :not-matched)))
        (is (= :matched (cond "" :matched true :not-matched)))
        (is (= :matched (cond "false" :matched true :not-matched)))
        (is (= :matched (cond :false :matched true :not-matched)))
        (is (= :matched (cond [] :matched true :not-matched)))
        (is (= :matched (cond '() :matched true :not-matched)))
        (is (= :matched (cond {} :matched true :not-matched)))
        (is (= :matched (cond #{} :matched true :not-matched)))
        (is (= :matched (cond identity :matched true :not-matched)))))

    (testing "clauses are tried in order and the first true test wins"
      (is (= :a (cond true :a true :b)))
      (is (= :b (cond false :a true :b true :c)))
      (is (= :d (cond false :a nil :b (= 1 2) :c (= 1 1) :d)))
      (is (= :negative (cond (< -1 0) :negative
                              (zero? -1) :zero
                              :else :positive)))
      (is (= :zero (cond (< 0 0) :negative
                         (zero? 0) :zero
                         :else :positive)))
      (is (= :positive (cond (< 1 0) :negative
                             (zero? 1) :zero
                             :else :positive)))
      (is (= :positive (cond (< 101 0) :negative
                             (zero? 101) :zero
                             :else :positive))))

    (testing "the :else convention"
      (is (= :default (cond false :a nil :b :else :default)))
      (is (= :matched (cond :else :matched))))

    (testing "evaluation is lazy and short-circuits"
      (testing "tests are evaluated one at a time, in order, until one is true"
        (let [log (volatile! [])]
          (is (= :b
                 (cond
                   (do (vswap! log conj :test-1) false) (do (vswap! log conj :expr-1) :a)
                   (do (vswap! log conj :test-2) true) (do (vswap! log conj :expr-2) :b)
                   (do (vswap! log conj :test-3) true) (do (vswap! log conj :expr-3) :c))))
          (is (= [:test-1 :test-2 :expr-2] @log))))
      (testing "later tests and exprs are not evaluated"
        (is (= :a (cond true :a :else (throw (ex-info "unreachable" {}))))))
      (testing "does not realize lazy sequences"
        (is (= :matched (cond (range) :matched)))
        (is (= [0 1 2] (take 3 (cond false :a :else (range)))))))

    ;; cond checks its clauses as it expands, so an odd number of forms is
    ;; rejected during expansion. There's no eval in the CLJS test setup, so
    ;; this is only tested where eval is available.
    #?(:cljs nil
       :default
       (testing "an odd number of forms is rejected"
         (are [form] (p/thrown? (eval form))
           '(cond true)
           '(cond nil)
           '(cond false :a true)
           '(cond true :a false :b nil))))))
