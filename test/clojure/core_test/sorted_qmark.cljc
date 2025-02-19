(ns clojure.core-test.sorted-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/sorted?
  (deftest test-sorted?
    ;; Note that `sorted?` tests whether the collection is a sorted
    ;; collection, not whether the elements of any arbitrary
    ;; collection may or may not be sorted.
    (testing "positive cases"
      ;; Sorted maps and sorted sets are `sorted?`
      (is (sorted? (sorted-map)))
      (is (sorted? (sorted-set)))
      (is (sorted? (sorted-map-by <)))
      (is (sorted? (sorted-set-by <))))

    (testing "negative cases"
      ;; Most everyting else is not `sorted?`
      (are [coll] (not (sorted? coll))
        (hash-map)
        (array-map)
        (hash-set)
        (vector)
        (list)
        (sort (range 10 0 -1)) ; the items are sorted, but it's not a sorted collection
        (seq (sorted-map)) ; seqs, even over sorted collections are not sorted
        (seq (sorted-set))
        1                               ; primitives are not sorted
        1N
        1.0
        1.0M
        :a-keyword
        'a-sym
        "a string"
        true
        false
        (object-array 3)
        nil))))
