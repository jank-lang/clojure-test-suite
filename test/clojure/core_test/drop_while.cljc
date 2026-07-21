(ns clojure.core-test.drop-while
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists drop-while

  ;; Docstring:
  ;; ([pred] [pred coll])
  ;; Returns a lazy sequence of the items in coll starting from the
  ;; first item for which (pred item) returns logical false.  Returns a
  ;; stateful transducer when no collection is provided.

  (deftest test-drop-while
    (testing "arity-1 (stateful transducer)"
      (are [expected pred coll] (= expected (into [] (drop-while pred) coll))
        ;; basics
        (range 5 10) #(< % 5) (range 0 10)
        [1 2 3] keyword? [:a :b :c 1 2 3]
        [1 2 3] keyword? (list :a :b :c 1 2 3)
        [\o] #(not= % \o) "hello"
        [5 6 7 8 9] #(< % 5) (int-array [0 1 2 3 4 5 6 7 8 9])
        [[:c 3] [:d 4]] #(not= (first %) :c) (sorted-map :a 1 :b 2 :c 3 :d 4)
        [] (constantly true) {:a 1 :b 2 :c 3 :d 4}
        [:c :d] #(not= % :c) (sorted-set :a :b :c :d)
        [] (constantly true) #{:a :b :c :d}

        ;; constant predicates
        [] (constantly true) [:a :b :c 1 2 3]
        [] (constantly 1) [:a :b :c 1 2 3] ; `1` = truthy
        [:a :b :c 1 2 3] (constantly false) [:a :b :c 1 2 3]
        [:a :b :c 1 2 3] (constantly nil) [:a :b :c 1 2 3] ; `nil` = falsey

        ;; empty collections
        [] (constantly false) []
        [] (constantly false) '()
        [] (constantly false) nil
        [] (constantly false) ""
        [] (constantly false) (int-array [])
        [] (constantly true) []
        [] (constantly true) '()
        [] (constantly true) nil
        [] (constantly true) ""
        [] (constantly true) (int-array []))

      ;; Try it twice with the same `xf` to validate correct
      ;; internal state handling
      (let [xf (drop-while neg?)]
        (is (= [0 1 2] (sequence xf [-2 -1 0 1 2])))
        (is (= [0 1 2] (sequence xf [-1 0 1 2])))))

    (testing "arity-2 (lazy seq)"
      (are [expected pred coll] (= expected (drop-while pred coll))
        ;; basics
        (range 5 10) #(< % 5) (range 0 10)
        [1 2 3] keyword? [:a :b :c 1 2 3]
        [1 2 3] keyword? (list :a :b :c 1 2 3)
        [\o] #(not= % \o) "hello"
        [5 6 7 8 9] #(< % 5) (int-array [0 1 2 3 4 5 6 7 8 9])
        [[:c 3] [:d 4]] #(not= (first %) :c) (sorted-map :a 1 :b 2 :c 3 :d 4)
        [] (constantly true) {:a 1 :b 2 :c 3 :d 4}
        [:c :d] #(not= % :c) (sorted-set :a :b :c :d)
        [] (constantly true) #{:a :b :c :d}

        ;; constant predicates
        [] (constantly true) [:a :b :c 1 2 3]
        [] (constantly 1) [:a :b :c 1 2 3] ; `1` = truthy
        [:a :b :c 1 2 3] (constantly false) [:a :b :c 1 2 3]
        [:a :b :c 1 2 3] (constantly nil) [:a :b :c 1 2 3] ; `nil` = falsey

        ;; empty collections
        [] (constantly false) []
        [] (constantly false) '()
        [] (constantly false) nil
        [] (constantly false) ""
        [] (constantly false) (int-array [])
        [] (constantly true) []
        [] (constantly true) '()
        [] (constantly true) nil
        [] (constantly true) ""
        [] (constantly true) (int-array []))

      ;; Infinite lazy seq
      (let [s (drop-while #(< % 5) (range))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= 5 (first s)))))

    (testing "negative tests"
      ;; `nil` is not a predicate function
      (is (p/thrown? (doall (drop-while nil (range 0 10)))))
      (is (p/thrown? (into [] (drop-while nil) (range 0 10))))

      ;; not collections
      (is (p/thrown? (doall (drop-while (constantly false) 17))))
      (is (p/thrown? (doall (drop-while (constantly false) :a)))))))
