(ns clojure.core-test.map
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists map
  (deftest test-map
    (testing "arity 1"
      ;; Returns transducer
      (let [mfn (map inc)]
        ;; Is it a function?
        (is (fn? mfn))
        ;; Does it transduce correctly?
        (is (= [1 2 3 4 5] (transduce mfn conj (range 5))))
        ;; Can it be composed?
        (is (= [2 3 4 5 6] (transduce (comp mfn mfn) conj (range 5))))))

    (testing "arity 2"
      (let [result (map inc (range 5))]
        ;; `map` should return an unrealized lazy-seq
        (is (p/lazy-seq? result))
        (is (not (realized? result)))
        (is (= [1 2 3 4 5] result)))

      ;; nil and empty list just result in empty seq
      (let [try-nil (map identity nil)
            try-empty-list (map identity '())]
        (is (and (p/lazy-seq? try-nil) (empty? try-nil)))
        (is (and (p/lazy-seq? try-empty-list) (empty? try-empty-list))))

      ;; infinite lazy seq
      (let [s (map + (range))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [0 1 2 3 4] (take 5 s))))

      ;; Works on anything that can be turned into a seq, but order
      ;; isn't guaranteed for things like maps or sets
      (is (= [[:k :v]] (map identity {:k :v})))
      (is (= #{1 2 3 4 5} (set (map inc (range 5)))))
      (is (= [\a \b \c \d] (map identity "abcd"))))

    (testing "arity 3"
      ;; multiple seqs result in multiple args to the mapping function
      (let [s (map + (range 5) (range 5))]
        ;; returns an unrealized lazy seq
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [0 2 4 6 8] s)))

      ;; map terminates after the end of the shortest seq, whichever
      ;; one that is
      (is (= [0 2 4 6] (map + (range 5) (range 4))))
      (is (= [0 2 4 6] (map + (range 4) (range 5))))

      ;; Infinite ranges
      (let [s (map + (range) (range))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [0 2 4 6 8] (take 5 s))))

      ;; if one of the seqs is empty, map yields no elements
      (is (empty? (map + (range 5) nil)))
      (is (empty? (map + (range) nil)))
      (is (empty? (map + nil (range 5))))
      (is (empty? (map + nil (range))))
      (is (empty? (map + (range 5) '())))
      (is (empty? (map + (range) '())))
      (is (empty? (map + '() (range 5))))
      (is (empty? (map + '() (range))))
      (is (empty? (map + nil nil)))
      (is (empty? (map + '() '()))))

    (testing "arity 4"
      ;; multiple seqs result in multiple args to the mapping function
      (let [s (map + (range 5) (range 5) (range 5))]
        ;; returns an unrealized lazy seq
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [0 3 6 9 12] s)))

      ;; map terminates after the end of the shortest seq, whichever
      ;; one that is
      (is (= [0 3 6 9] (map + (range 5) (range 5) (range 4))))
      (is (= [0 3 6 9] (map + (range 5) (range 4) (range 5))))
      (is (= [0 3 6 9] (map + (range 4) (range 5) (range 5))))

      ;; Infinite ranges
      (is (= [0 3 6 9] (take 4 (map + (range) (range) (range)))))

      ;; if any of the seqs is `nil`, result is empty
      (is (empty? (map + (range 5) (range) nil)))
      (is (empty? (map + (range 5) nil (range))))
      (is (empty? (map + nil (range 5) (range))))
      ;; if any of the seqs is the empty list, result is empty
      (is (empty? (map + (range 5) (range) '())))
      (is (empty? (map + (range 5) '() (range))))
      (is (empty? (map + '() (range 5) (range)))))

    (testing "arity 5+"
      ;; multiple seqs result in multiple args to the mapping function
      (let [s (map + (range 5) (range 5) (range 5) (range 5))]
        ;; returns an unrealized lazy seq
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [0 4 8 12 16] s)))

      ;; map terminates after the end of the shortest seq, whichever
      ;; one that is
      (is (= [0 4 8 12] (map + (range 5) (range 5) (range 5) (range 4))))
      (is (= [0 4 8 12] (map + (range 5) (range 5) (range 4) (range 5))))
      (is (= [0 4 8 12] (map + (range 5) (range 4) (range 5) (range 5))))
      (is (= [0 4 8 12] (map + (range 4) (range 5) (range 5) (range 5))))

      ;; Infinite ranges
      (is (= [0 4 8 12] (take 4 (map + (range) (range) (range) (range)))))

      ;; if any of the seqs is `nil`, result is empty
      (is (empty? (map + (range 5) (range) (range 5) nil)))
      (is (empty? (map + (range 5) (range) nil (range 5))))
      (is (empty? (map + (range 5) nil (range) (range 5))))
      (is (empty? (map + nil (range 5) (range) (range 5))))
      ;; if any of the seqs is the empty list, result is empty
      (is (empty? (map + (range 5) (range) (range 5) '())))
      (is (empty? (map + (range 5) (range) '() (range 5))))
      (is (empty? (map + (range 5) '() (range) (range 5))))
      (is (empty? (map + '() (range 5) (range) (range 5))))

      ;; Try five collections
      (is (= [0 5 10 15 20] (map + (range 5) (range 5) (range 5) (range 5) (range 5))))

      ;; Five infinite ranges
      (is (= [0 5 10 15 20] (take 5 (map + (range) (range) (range) (range) (range)))))

      ;; Try with infinite ranges except for one
      (is (= [0 5 10 15 20] (map + (range) (range) (range 5) (range) (range)))))

    (testing "negative cases"
      ;; Note: must realize resulting lazy-seq to force the
      ;; exception
      (are [x] (p/thrown? (doall (map identity x)))
        ;; map works over all seqs, but these aren't seqs
        1
        true
        false
        #?@(;; Chars aren't seqs except in CLJS and Basilisp where char is string of length 1
            :cljs []
            :lpy  []
            :default [\a])
        :a
        'a))))
