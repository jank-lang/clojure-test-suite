(ns clojure.core-test.partition
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists partition
  (deftest test-partition

    ;; Docstring:
    ;; [n coll] [n step coll] [n step pad coll]
    ;; Returns a lazy sequence of lists of n items each, at offsets step
    ;; apart. If step is not supplied, defaults to n, i.e. the partitions
    ;; do not overlap. If a pad collection is supplied, use its elements as
    ;; necessary to complete last partition upto n items. In case there are
    ;; not enough padding elements, return a partition with less than n items.

    (testing "arity 2 - (partition n coll)"
      (let [s (partition 2 (range 10))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= '((0 1) (2 3) (4 5) (6 7) (8 9)) s)))

      ;; Infinite range
      (is (= '((0 1) (2 3) (4 5) (6 7)) (take 4 (partition 2 (range)))))

      ;; empty list and nil for the collection
      (is (= '() (partition 2 '())))
      (is (= '() (partition 2 nil))))

    (testing "arity 3 - (partition n step coll)"
      ;; Step by 1 instead of default 2
      ;; Note that (range 6) stops before allowing a partial partition.
      ;; Note that we tried testing non-positive steps, but that led
      ;; to an infinite loop within `partition`.
      (is (= '((0 1) (1 2) (2 3) (3 4) (4 5)) (partition 2 1 (range 6))))

      ;; Try an infinite range
      (is (= '((0 1) (1 2) (2 3) (3 4) (4 5)) (take 5 (partition 2 1 (range)))))

      ;; empty list and nil
      (is (= '() (partition 2 1 '())))
      (is (= '() (partition 2 1 nil))))

    (testing "arity 4 - (partition n step pad coll"
      ;; Use padding for the last element
      (is (= '((0 1 2) (1 2 3) (2 3 4) (3 4 :a)) (partition 3 1 [:a :a :a] (range 5))))
      (is (= '((0 1 2) (3 4 5) (6 7 8) (9 :a :a))  (partition 3 3 [:a :a :a] (range 10))))

      ;; empty list and nil
      (is (= '() (partition 3 1 [:a :a :a] '())))
      (is (= '() (partition 3 1 [:a :a :a] nil))))))
