(ns clojure.core-test.map-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists map?
  (deftest test-map?
    (are [expected x] (= expected (map? x))
      true (hash-map :a 1)

      #?@(:lpy []
          :default
          [true (array-map :a 1)
           true (sorted-map :a 1)])

      false [1 2 3]
      false '(1 2 3)
      false (hash-set :a)
      false (seq [1 2 3])
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
      false (object-array 3)

      #?@(:lpy []
          :default
          [false (sorted-set :a)
           false (seq (sorted-map :a 1))
           false (seq (sorted-set :a))]))))
