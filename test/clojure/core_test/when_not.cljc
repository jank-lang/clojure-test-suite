(ns clojure.core-test.when-not
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-not
  (deftest test-when-not
    (testing "executes body when condition is false"
      (is (= 42 (when-not false 42)))
      (is (= :result (when-not false :result))))

    (testing "returns nil when condition is true"
      (is (nil? (when-not true 42)))
      (is (nil? (when-not true :result))))

    (testing "with nil condition (falsey)"
      (is (= 42 (when-not nil 42))))

    (testing "with truthy values"
      (is (nil? (when-not 1 42)))
      (is (nil? (when-not "string" 42)))
      (is (nil? (when-not [] 42)))
      (is (nil? (when-not {} 42)))
      (is (nil? (when-not :keyword 42))))

    (testing "multiple expressions in body"
      (is (= 3 (when-not false 1 2 3)))
      (is (= :last (when-not false :first :second :last))))

    (testing "side effects"
      (let [a (atom 0)]
        (when-not false
          (swap! a inc)
          (swap! a inc))
        (is (= 2 @a)))
      (let [a (atom 0)]
        (when-not true
          (swap! a inc))
        (is (= 0 @a))))

    (testing "nested when-not"
      (is (= 42 (when-not false
                  (when-not false 42))))
      (is (nil? (when-not false
                  (when-not true 42)))))

    (testing "with comparison expressions"
      (is (= :yes (when-not (= 1 2) :yes)))
      (is (nil? (when-not (= 1 1) :yes)))
      (is (= :yes (when-not (> 1 2) :yes)))
      (is (nil? (when-not (< 1 2) :yes))))

    (testing "with predicate functions"
      (is (= :yes (when-not (even? 3) :yes)))
      (is (nil? (when-not (even? 2) :yes)))
      (is (= :yes (when-not (empty? [1]) :yes)))
      (is (nil? (when-not (empty? []) :yes))))

    (testing "returns last expression"
      (is (= 10 (when-not false
                  (+ 1 2)
                  (+ 3 4)
                  (+ 5 5)))))

    (testing "with computed condition"
      (is (= :result (when-not (< 5 3) :result)))
      (is (nil? (when-not (> 5 3) :result))))

    (testing "with boolean logic"
      (is (= :yes (when-not (and false true) :yes)))
      (is (nil? (when-not (or true false) :yes)))
      (is (= :yes (when-not (not true) :yes))))

    (testing "empty body"
      (is (nil? (when-not false)))
      (is (nil? (when-not true))))

    (testing "with collection operations"
      (is (= [1 2 3] (when-not (empty? [1 2 3]) [1 2 3])))
      (is (nil? (when-not (seq [1 2 3]) [1 2 3]))))

    (testing "single expression body"
      (is (= 42 (when-not false 42)))
      (is (nil? (when-not true 42))))

    (testing "opposite of when"
      (is (= (when false 42) (when-not true 42)))
      (is (= (when true 42) (when-not false 42))))

    (testing "with nil return value"
      (is (nil? (when-not false nil)))
      (is (nil? (when-not true nil))))

    (testing "sequential execution"
      (let [result (atom [])]
        (when-not false
          (swap! result conj 1)
          (swap! result conj 2)
          (swap! result conj 3))
        (is (= [1 2 3] @result))))))
