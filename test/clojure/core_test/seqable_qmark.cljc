(ns clojure.core-test.seqable-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists seqable?
  (deftest test-seqable?
    (are [expected x] (= expected (seqable? x))
      true [1 2 3]
      true (sorted-map :a 1)
      true (sorted-set :a)
      true '(1 2 3)
      true (hash-map :a 1)
      true (array-map :a 1)
      true (hash-set :a)
      true (seq [1 2 3])
      true (seq (sorted-map :a 1))
      true (seq (sorted-set :a))
      true (range 0 10)
      true (range)
      true nil
      true "a string"
      true (object-array 3)

      false 1
      false 1N
      false 1.0
      false 1.0M
      false :a-keyword
      false 'a-sym
      #?(:cljs true :default false) \a)))
