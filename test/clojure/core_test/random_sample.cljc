(ns clojure.core-test.random-sample
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists random-sample

  (defn check
    [nitems xs]
    ;; check that subsets are not constant
    (is (> (count (set xs)) 1))
    ;; check that every subset has a size between 0 and the size of
    ;; the original collection (nitems)
    (is (every? #(and (>= (count %) 0)
                      (< (count %) nitems))
                xs))
    ;; check that every item of every subset is an item in the
    ;; original collection
    (is (every? (fn [sub]
                  (every? (fn [item]
                            (and (>= item 0)
                                 (< item nitems)))
                          sub))
                xs)))
  
  (deftest test-random-sample
    ;; Multiple calls to random-sample should return non-constant
    ;; subsets If all the items in the collection are unique, every
    ;; item in the subset should be unique and be one of the items in
    ;; the collection. Length of each subset should be between 0 and
    ;; the length of the original collection.
    (let [draws 10
          nitems 10000
          coll (doall (range nitems))
          prob 0.5]
      (testing "positive tests"
        (check nitems (repeatedly draws #(random-sample prob coll)))
        (check nitems (repeatedly draws #(transduce (random-sample prob) conj [] coll)))
        ;; if probability is 0, then the result is always an empty seq
        (is (every? (comp nil? seq) (repeatedly draws #(random-sample 0 coll))))
        (is (every? (comp nil? seq) (repeatedly draws #(transduce (random-sample 0) conj [] coll))))
        ;; if probability is 1, then the result is always the input collection
        (is (every? #(= % coll) (repeatedly draws #(random-sample 1 coll))))
        (is (every? #(= % coll) (repeatedly draws #(transduce (random-sample 1) conj [] coll))))
        ;; if input collection is empty, then the result is always empty
        (is (every? (comp nil? seq) (repeatedly draws #(random-sample 1 []))))
        (is (every? (comp nil? seq) (repeatedly draws #(transduce (random-sample 1) conj [] [])))))

      (testing "negative tests"
        ;; if probablity is < 0, always empty
        (is (every? (comp nil? seq) (repeatedly draws #(random-sample -1 coll))))
        (is (every? (comp nil? seq) (repeatedly draws #(transduce (random-sample -1) conj [] coll))))
        ;; if probability is > 1, then the result is always the input collection
        (is (every? #(= % coll) (repeatedly draws #(random-sample 10 coll))))
        (is (every? #(= % coll) (repeatedly draws #(transduce (random-sample 10) conj [] coll))))
        ;; if nil as input collection, then the result is always empty
        (is (every? (comp nil? seq) (repeatedly draws #(random-sample -1 nil))))
        (is (every? (comp nil? seq) (repeatedly draws #(transduce (random-sample -1) conj [] nil))))
        
        #?(:cljs (is (nil? (seq (random-sample nil coll))))
           :default (is (thrown? Exception (seq (random-sample nil coll)))))
        (is (thrown? #?(:cljs :default :default Exception) (seq (random-sample 0.5 42))))
        (is (thrown? #?(:cljs :default :default Exception) (seq (random-sample 0.5 :foo))))))))
