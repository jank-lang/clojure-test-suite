(ns clojure.core-test.lazy-seq
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists lazy-seq

  ;; Note: we cannot use Clojure's standard `range` here because
  ;; Clojure JVM uses a `clojure.lang.LongRange` which is chunked and
  ;; is not a lazy seq, in spite of what the `range` doc string says.
  (defn lazy-infinite-range
    ([] (lazy-infinite-range 0))
    ([n] (lazy-seq (cons n (lazy-infinite-range (inc n))))))

  (defn nthrest*
    "The standard version of `nthrest` calls `seq` at a certain point and
  forces lazy seqs to become partially realized. This custom version
  only calls `rest` on the collection the number of times specified by
  `n`."
    [coll n]
    (assert (not (neg? n)))
    (loop [n n
           coll coll]
      (if (zero? n)
        coll
        (recur (dec n) (rest coll)))))

  (deftest test-lazy-seq
    ;; Note: lazy seqs are stateful. Once a lazy seq is realized, it
    ;; cannot be unrealized. Holding a reference to the lazy seq from
    ;; when it was unrealized doesn't prevent the reference from
    ;; seeing its realized state.
    (testing "basics"
      ;; Super short lazy seq
      (let [s (lazy-seq (cons 1 nil))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1] (doall s)))
        (is (realized? s)))

      ;; The realized value of a lazy seq just has to be a seq, not
      ;; necessarily a cons, so we can return a whole vector.
      (let [s (lazy-seq [1 2 3])]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1 2 3] (doall s)))
        (is (realized? s)))

      (let [s (lazy-infinite-range)]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= 0 (first s)))
        (is (realized? s))               ; Calling `first` realizes lazy seq
        (is (p/lazy-seq? (rest s)))      ; but the rest remains lazy
        (is (not (realized? (rest s))))  ; and unrealized.
        (is (p/lazy-seq? (nthrest* s 50))) ; nthrest* forces realization of everything before
        (is (not (realized? (nthrest* s 50))))
        (is (p/lazy-seq? (nthrest* s 49)))
        (is (realized? (nthrest* s 49))))

      ;; Remember `(next coll)` = `(seq (rest coll))` and `seq`
      ;; realizes the element and returns a seq rather than a lazy
      ;; seq.
      (let [s (lazy-infinite-range)]
        (next s)
        (is (realized? (rest s))) ; `next` forces realization of next element
        (is (p/lazy-seq? (nthrest* s 10)))
        (is (not (realized? (nthrest* s 10))))))  ; but further elements not realized

    (testing "oddball cases"
      ;; You can have lazy seqs wrapping lazy seqs. In this case, when
      ;; the lazy seq is realized, Clojure will keep realizing lazy
      ;; seqs until it bottoms out with something that is not a lazy
      ;; seq
      (let [s (lazy-seq
               (lazy-seq
                (lazy-seq
                 (cons 1 (lazy-seq
                          (lazy-seq
                           (lazy-seq
                            (lazy-seq (cons 2 (lazy-seq
                                               (lazy-seq
                                                (lazy-seq
                                                 (lazy-seq
                                                  (lazy-seq (cons 3 nil)))))))))))))))]
        (is (= 1 (first s)))
        (is (= 2 (first (next s))))
        (is (= 3 (first (next (next s))))))

      ;; `nil` is a seq
      (is (nil? (first (lazy-seq nil))))
      (is (= '() (rest (lazy-seq nil))))
      (is (nil? (next (lazy-seq nil))))

      ;; the empty list is also a seq
      (is (nil? (first (lazy-seq '()))))
      (is (= '() (rest (lazy-seq '()))))
      (is (nil? (next (lazy-seq '())))))

    (testing "negative cases"
      ;; The realized value of `lazy-seq` must be a seq (i.e., satisfy
      ;; `ISeq` or its dialect-specific equivalent)
      (is (p/thrown? (first (lazy-seq 1))))) ; a long is not a seq
    ))
