(ns clojure.core-test.keep
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/keep

  ;; DOCSTRING:
  ;; clojure.core/keep
  ;; [f]
  ;; [f coll]
  ;; Returns a lazy sequence of the non-nil results of (f item). Note,
  ;; this means false return values will be included.  f must be free of
  ;; side-effects.  Returns a transducer when no collection is provided.

  ;; `keep` itself only distinguishes a `nil` result from a non-nil one:
  ;; which non-nil value `f` returned, and how it decided that, is
  ;; irrelevant. So the functions below are deliberately trivial (mostly
  ;; `(case ... nil ...)`) - the point is what `keep` does with a `nil`
  ;; result versus a non-nil one.
  (deftest test-keep
    (testing "arity-1 (transducer)"
      (let [xf (keep #(case % :drop nil %))]
        ;; It's a function...
        (is (fn? xf))
        ;; ...that passes on non-nil results and drops nil ones
        (is (= [1 3] (into [] xf [1 :drop 3])))
        (is (= [1 3] (transduce xf conj [1 :drop 3])))
        (is (= [1 3] (sequence xf [1 :drop 3])))
        ;; an empty or `nil` source yields nothing
        (is (= [] (into [] xf [])))
        (is (= [] (into [] xf nil))))

      (testing "the non-nil results of f are passed on, not the items"
        (is (= [[:a] [:c]] (into [] (keep #(case % :drop nil [%]))
                                 [:a :drop :c]))))

      (testing "non-nil `false` results are kept"
        (is (= [false] (into [] (keep #(case % :keep false nil)) [:keep :drop])))
        (is (= [false] (transduce (keep #(case % :keep false nil)) conj [:keep :drop]))))

      (testing "composition with other transducers"
        (is (= [3] (into [] (comp (keep #(case % 2 nil %))
                                  (keep #(case % 1 nil %)))
                         [1 2 3])))
        (is (= [2] (into [] (comp (keep #(case % 1 nil %)) (take 1)) [1 2 3]))))

      ;; a transducer cannot realize more of an infinite source than it needs
      (is (= [0 1 2] (into [] (comp (keep identity) (take 3)) (range))))

      ;; `keep` is stateless, so the same transducer can be reused
      (let [xf (keep #(case % :drop nil %))]
        (is (= [1 3] (into [] xf [1 :drop 3])))
        (is (= [4 5] (into [] xf [4 :drop 5]))))

      ;; results pass through untouched, metadata included
      (let [m (with-meta [:m] {:k 1})]
        (is (identical? m (first (into [] (keep identity) [m]))))
        (is (= {:k 1} (meta (first (into [] (keep identity) [m])))))))

    (testing "arity-2 (lazy seq)"
      (let [res (keep #(case % :drop nil %) [:a :drop :b])]
        ;; `keep` returns an unrealized lazy seq
        (is (p/lazy-seq? res))
        (is (not (realized? res)))
        (is (= [:a :b] res)))

      (testing "the non-nil results of f are returned, not the items"
        (is (= ["1" "2"] (keep str [1 2])))
        (is (= [[1] [2]] (keep vector [1 2]))))

      (testing "nil results are dropped"
        (is (= [] (keep (constantly nil) [1 2 3])))
        (is (= [1 2] (keep identity [1 nil 2]))))

      (testing "non-nil `false` results are kept"
        (is (= [false false] (keep identity [false false])))
        (is (= [false false false] (keep (constantly false) [1 2 3]))))

      (testing "nil and empty collections"
        (are [coll] (= [] (keep identity coll))
          nil
          []
          '()
          ""
          (range 0)))

      (testing "seqable non-vector collections"
        (is (= [1 2 3] (keep identity (range 1 4))))
        (is (= [\a \b \c] (keep identity "abc")))
        (is (= [1 2 3] (keep identity (sorted-set 1 2 3)))))

      (testing "infinite collection"
        (let [s (keep identity (range))]
          (is (p/lazy-seq? s))
          (is (not (realized? s)))
          (is (= [0 1 2] (take 3 s))))
        ;; only the results needed are computed; a nil-returning `f` does
        ;; not stop the walk
        (is (= [1 2 3] (take 3 (keep identity (iterate inc 1))))))

      (testing "results are returned as-is, metadata included"
        (let [a (with-meta [:a] {:k 1})
              b (with-meta [:b] {:k 2})
              res (keep identity [a nil b])]
          (is (= [[:a] [:b]] res))
          (is (identical? a (first res)))
          (is (identical? b (second res)))
          (is (= {:k 1} (meta (first res))))
          (is (= {:k 2} (meta (second res)))))))

    (testing "negative cases"
      ;; Note: the resulting lazy-seq must be realized to force the
      ;; exception
      
      ;; bad functions
      (are [f] (p/thrown? (doall (keep f [1 2 3])))
        nil
        1
        "a"
        true)

      ;; bad collections
      (are [coll] (p/thrown? (doall (keep identity coll)))
        1
        true
        :a
        'a)

      ;; `nil` is not a function for the transducer case
      (is (p/thrown? (into [] (keep nil) [1 2 3])))
      (is (p/thrown? (transduce (keep nil) conj [1 2 3]))))))
