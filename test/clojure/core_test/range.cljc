(ns clojure.core-test.range
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/range

  ;; DOCSTRING:
  ;; clojure.core/range
  ;; []
  ;; [end]
  ;; [start end]
  ;; [start end step]
  ;; Returns a lazy seq of nums from start (inclusive) to end
  ;; (exclusive), by step, where start defaults to 0, step to 1, and end to
  ;; infinity. When step is equal to 0, returns an infinite sequence of
  ;; start. When start is equal to end, returns empty list.

  (deftest test-range
    (testing "arity 0 - start 0, step 1, end infinity"
      (is (= [0 1 2 3 4] (take 5 (range))))
      ;; Infinite, but elements are produced on demand rather than
      ;; realizing the whole (unbounded) sequence.
      (is (= 1000 (nth (range) 1000)))
      (is (= 10000 (count (range 10000)))))

    (testing "arity 1 - start defaults to 0, step to 1"
      (are [expected end] (= expected (range end))
        []          0
        []          -5
        [0]         1
        [0 1 2 3 4] 5))

    (testing "arity 2 - step defaults to 1"
      (are [expected start end] (= expected (range start end))
        [2 3 4]        2  5
        [0 1 2 3 4]    0  5
        [-3 -2 -1 0 1] -3 2
        []             5  2    ; step is +1, so it walks away from end
        []             3  3)) ; start = end

    (testing "arity 3"
      (are [expected start end step] (= expected (range start end step))
        [0 3 6 9]  0  10 3
        [0 3]      0  6  3 ; end is exclusive
        [1 4 7]    1  10 3
        [0]        0  3  10
        [10 7 4 1] 10 0  -3
        []         0  5  -1 ; wrong direction
        [-1 -2 -3] -1 -4 -1
        []         5  0  1
        []         3  3  1))

    (testing "step 0 gives an infinite sequence of start"
      (is (= [0 0 0 0 0] (take 5 (range 0 5 0))))
      (is (= [2 2 2] (take 3 (range 2 3 0))))
      ;; start = end wins over the zero step
      (is (= [] (range 3 3 0))))

    (testing "non-integer arguments"
      (is (= [0.0 0.5 1.0 1.5 2.0] (range 0.0 2.5 0.5)))
      (is (= [1.0 0.5] (range 1.0 0 -0.5)))
      ;; CLJS doesn't have ratios
      #?@(:cljs []
          :default [(is (= [0 1/4 1/2 3/4] (range 0 1 1/4)))
                    (is (= [1 1/2] (range 1 0 -1/2)))]))

    (testing "returns a seq"
      (is (seq? (range))))

    (testing "negative tests"
      ;; Non-numeric arguments
      #?(:cljs
         ;; CLJS coerces non-numbers to 0 instead of throwing, so a nil
         ;; step behaves like the zero-step case.
         (is (= [0 0 0] (take 3 (range 0 5 nil))))
         :default
         (do
           (is (p/thrown? (range nil)))
           (is (p/thrown? (range 0 5 nil))))))))
