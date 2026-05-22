(ns clojure.core-test.concat
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists concat
  (deftest test-concat
    (testing "arity 0"
      ;; returns an unrealized, empty lazy seq
      (let [s (concat)]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (empty? s))))

    (testing "arity 1"
      ;; returns an unrealized lazy seq of just the elements in the
      ;; collection
      (let [s (concat '(1 2 3 4 5))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1 2 3 4 5] s)))

      ;; Make sure that `concat` doesn't try to realize an infinite
      ;; `range`
      (is (= [0 1 2 3 4] (take 5 (concat (range))))))

    (testing "arity 2"
      ;; Let's actually concatenate two things
      (let [s (concat '(1 2 3 4 5) [6 7 8 9 10])] ; anything seqable
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1 2 3 4 5 6 7 8 9 10] s)))

      ;; Infinite range as second argument.
      (is (= [1 2 3 0 1 2] (take 6 (concat [1 2 3] (range)))))
      ;; Infinite range as first argument, so we'll never take
      ;; elements from the second argument.
      (is (= [0 1 2 3 4] (take 5 (concat (range) [:a :b :c])))))

    (testing "arity 3+"
      ;; Concatenate four things: vector, range, list, infinite range
      (let [s (concat [0 1 2] (range 3 10) '(10 11 12) (range))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        ;; Now, take the first 16 items from the concatenation
        (is (= [0 1 2 3 4 5 6 7 8 9 10 11 12 0 1 2] (take 16 s))))

      ;; Go crazy with ranges, empty list, and nil
      (is (= [0 1 2 3 4 5 0 1 2 3 4 5 6 7 8]
             (take 15 (concat (range 3) nil (range 3 6) '() (range))))))

    (testing "empty list and nil"
      (is (= [1 2 3] (concat [1 2 3] '())))
      (is (= [1 2 3] (concat '() [1 2 3])))
      (is (= [1 2 3] (concat [1 2 3] nil)))
      (is (= [1 2 3] (concat nil [1 2 3]))))))
