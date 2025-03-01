(ns clojure.core-test.vector-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/vector?
  (deftest test-vector?
    (are [expected x] (= expected (vector? x))
      true [1 2 3]
      true (first {:a 1})               ; map entries are also vectors
      false (sorted-map :a 1)
      false (sorted-set :a)
      false '(1 2 3)
      false (hash-map :a 1)
      false (array-map :a 1)
      false (hash-set :a)
      false (seq [1 2 3])
      false (seq (sorted-map :a 1))
      false (seq (sorted-set :a))
      false (range 0 10)
      false (range)
      false nil
      false 1
      false 1N
      false 1.0
      false 1.0M
      false :a-keyword
      false 'a-sym
      false "a string"
      false \a
      false (object-array 3))))
