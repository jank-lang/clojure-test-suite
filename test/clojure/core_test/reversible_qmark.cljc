(ns clojure.core-test.reversible-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/reversible?
  (deftest test-reversible?
    (are [expected x] (= expected (reversible? x))
      true [1 2 3]
      true (sorted-map :a 1)
      true (sorted-set :a)
      false '(1 2 3)
      false (hash-map :a 1)
      false (array-map :a 1)
      false (hash-set :a)
      #?(:cljs true :default false) (seq [1 2 3])
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
