(ns clojure.core-test.nth
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists nth

  ;; Docstring:
  ;; ([coll index] [coll index not-found])
  ;; Returns the value at the index. get returns nil if index out of
  ;; bounds, nth throws an exception unless not-found is supplied.  nth
  ;; also works for strings, Java arrays, regex Matchers and Lists, and,
  ;; in O(n) time, for sequences.

  (deftest test-nth
    (testing "arity-2"
      ;; `nth` should be able to access elements in a number of
      ;; different collection types.
      (are [expected coll index] (= expected (nth coll index))
        ;; Lazy seqs
        0 (range 0 10) 0
        5 (range 0 10) 5
        10 (range) 10                   ; infinite lazy seq

        ;; vectors
        0 [0 1 2 3 4 5 6 7 8 9] 0
        5 [0 1 2 3 4 5 6 7 8 9] 5
        9 [0 1 2 3 4 5 6 7 8 9] 9

        ;; lists
        0 (list 0 1 2 3 4 5 6 7 8 9) 0
        5 (list 0 1 2 3 4 5 6 7 8 9) 5
        9 (list 0 1 2 3 4 5 6 7 8 9) 9

        ;; strings
        \h "hello" 0
        \l "hello" 2
        \o "hello" 4

        ;; arrays
        0 (int-array [0 1 2 3 4 5 6 7 8 9]) 0
        5 (int-array [0 1 2 3 4 5 6 7 8 9]) 5
        9 (int-array [0 1 2 3 4 5 6 7 8 9]) 9

        ;; Surprisingly, `nil` collection returns `nil`, regardless of
        ;; `index`
        nil nil 0
        nil nil 10
        nil nil -1)

      ;; re-matcher (NOTE: NOT re-matches)
      ;; re-matchers are stateful, so set it up and then force the
      ;; matching with `re-find`.
      #?(:cljs nil ; CLJS doesn't have re-matcher
         :lpy nil ; Basilisp doesn't have re-matcher
         :default (let [m (re-matcher #"(\d+),(\d+),(\d+)" "123,456,789")]
                    (re-find m)
                    (are [expected coll index] (= expected (nth coll index))
                      "123,456,789" m 0
                      "456" m 2
                      "789" m 3)
                    (is (p/thrown? (nth m 10)))))

      ;; with arity-2, `nth` throws if index > count of elements
      (are [coll index] (p/thrown? (nth coll index))
        (range 10) 20
        [] 0
        [] 10
        [1 2 3] 10
        (list) 0
        (list) 10
        (list 1 2 3) 10
        "" 0
        "" 10
        "abc" 10
        (int-array []) 0
        (int-array []) 10
        (int-array [1 2 3]) 10))

    (testing "arity-3"
      ;; `nth` should be able to access elements in a number of
      ;; different collection types.
      (are [expected coll index default] (= expected (nth coll index default))
        ;; Lazy seqs
        0 (range 0 10) 0 :default
        5 (range 0 10) 5 :default
        10 (range) 10 :default                   ; infinite lazy seq

        ;; vectors
        0 [0 1 2 3 4 5 6 7 8 9] 0 :default
        5 [0 1 2 3 4 5 6 7 8 9] 5 :default
        9 [0 1 2 3 4 5 6 7 8 9] 9 :default

        ;; lists
        0 (list 0 1 2 3 4 5 6 7 8 9) 0 :default
        5 (list 0 1 2 3 4 5 6 7 8 9) 5 :default
        9 (list 0 1 2 3 4 5 6 7 8 9) 9 :default

        ;; strings
        \h "hello" 0 :default
        \l "hello" 2 :default
        \o "hello" 4 :default

        ;; arrays
        0 (int-array [0 1 2 3 4 5 6 7 8 9]) 0 :default
        5 (int-array [0 1 2 3 4 5 6 7 8 9]) 5 :default
        9 (int-array [0 1 2 3 4 5 6 7 8 9]) 9 :default

        ;; When collection is `nil`, returns `:default`
        :default nil 0 :default
        :default nil 10 :default
        :default nil -1 :default)

      ;; re-matcher (NOTE: NOT re-matches)
      ;; re-matchers are stateful, so set it up and then force the
      ;; matching with `re-find`.
      #?(:cljs nil ; CLJS doesn't have re-matcher
         :lpy nil ; Basilisp doesn't have re-matcher
         :default (let [m (re-matcher #"(\d+),(\d+),(\d+)" "123,456,789")]
                    (re-find m)
                    (are [expected coll index] (= expected (nth coll index))
                      "123,456,789" m 0
                      "456" m 2
                      "789" m 3)
                    (is (= :default (nth m 10 :default)))))

      ;; with arity-3, `nth` returns the default value when index > count
      (are [expected coll index default] (= expected (nth coll index default))
        :default (range 10) 20 :default
        :default [] 0 :default
        :default [] 10 :default
        :default [1 2 3] 10 :default
        :default (list) 0 :default
        :default (list) 10 :default
        :default (list 1 2 3) 10 :default
        :default "" 0 :default
        :default "" 10 :default
        :default "abc" 10 :default
        :default (int-array []) 0 :default
        :default (int-array []) 10 :default
        :default (int-array [1 2 3]) 10 :default))

    (testing "negative cases"
      ;; doesn't work on maps or sets
      (is (p/thrown? (nth {:a 1 :b 2} 0)))
      (is (p/thrown? (nth #{:a :b :c :d} 0)))
      ;; but does work if you explicitly convert to seqs
      ;; Sorted collections are not available in every dialect (e.g. Basilisp);
      ;; sort a plain map/set's elements to control order there instead.
      #?(:lpy (is (= [:a 1] (nth (sort-by first (seq {:a 1 :b 2})) 0)))
          :default (is (= [:a 1] (nth (seq (sorted-map :a 1 :b 2)) 0)))) ; we use sorted maps and sets here
      #?(:lpy (is (= :a (nth (sort (seq #{:a :b :c :d})) 0)))
          :default (is (= :a (nth (seq (sorted-set :a :b :c :d)) 0)))) ; to control order

      (is (p/thrown? (nth [0 1 2] nil)))

      ;; Try negative `index` and both `coll` and `index` equal to `nil`
      #?@(:lpy
          [(is (= 2 (nth [0 1 2] -1)))  ; wrap around
           (is (= 1 (nth [0 1] -1 :default)))
           (is (= nil (nth nil nil)))]
          :default
          [(is (p/thrown? (nth [0 1 2] -1)))
           (is (= :default (nth [0 1] -1 :default)))
           (is (p/thrown? (nth nil nil)))]))))
