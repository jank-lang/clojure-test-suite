(ns clojure.core-test.filter
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/filter

  ;; DOCSTRING:
  ;; clojure.core/filter
  ;; [pred]
  ;; [pred coll]
  ;; Returns a lazy sequence of the items in coll for which
  ;; (pred item) returns logical true. pred must be free of side-effects.
  ;; Returns a transducer when no collection is provided.

  (deftest test-filter
    (testing "arity-1 (transducer)"
      (let [xf (filter odd?)]
        ;; It's a function...
        (is (fn? xf))
        ;; ...that transduces correctly
        (is (= [1 3 5] (into [] xf (range 6))))
        (is (= [1 3 5] (transduce xf conj (range 6))))
        (is (= [1 3 5] (sequence xf (range 6))))
        ;; `filter` transducers compose with other transducers
        (is (= [3 5] (into [] (comp (filter odd?) (filter #(< 2 %))) (range 6))))
        ;; ...and can limit realization of an infinite source
        (is (= [0 2 4] (into [] (comp (filter even?) (take 3)) (range)))))

      ;; `filter` is stateless, so the same transducer can be reused
      (let [xf (filter odd?)]
        (is (= [1 3] (into [] xf [1 2 3])))
        (is (= [1 3] (into [] xf [1 2 3]))))

      (are [expected pred coll] (= expected (into [] (filter pred) coll))
        [0 2 4] even? (range 6)
        [] even? nil
        [] even? []
        [] even? '()
        [] even? ""
        [\a \c] #(not= \b %) "abc"
        [[:a 1] [:c 3]] #(odd? (val %)) (sorted-map :a 1 :b 2 :c 3)
        [] (constantly false) [1 2 3]
        [1 2 3] (constantly true) [1 2 3]))

    (testing "arity-2 (lazy seq)"
      (let [res (filter even? (range 5))]
        ;; `filter` returns an unrealized lazy seq
        (is (p/lazy-seq? res))
        (is (not (realized? res)))
        (is (= [0 2 4] res)))

      (are [expected pred coll] (= expected (filter pred coll))
        ;; basics
        [0 2] even? (range 4)
        [:a :b] keyword? [:a 1 :b 2]
        [1 2 3] number? [\a 1 \b 2 3]
        [\h \e \o] #(not= \l %) "hello"
        [[:a 1] [:c 3]] #(odd? (val %)) (sorted-map :a 1 :b 2 :c 3)
        [:b] #(= :b %) (sorted-set :a :b :c)
        ;; constant predicates
        [] (constantly false) [:a :b :c]
        [:a :b :c] (constantly true) [:a :b :c]
        [] (constantly nil) [:a :b :c]
        ;; any logically true value keeps the item
        [:a :b :c] (constantly 1) [:a :b :c]
        [:a :b :c] (constantly "yes") [:a :b :c]
        ;; nil and empty collections
        [] even? nil
        [] even? []
        [] even? '()
        [] even? ""
        [] even? (range 0))

      (testing "predicate result is interpreted as logical truth"
        (is (= [:a :b] (filter identity [:a nil :b false])))
        (is (= [] (filter identity [nil false])))
        (is (= [0 ""] (filter identity [0 ""]))))

      (testing "infinite collection"
        (let [s (filter even? (range))]
          (is (p/lazy-seq? s))
          (is (not (realized? s)))
          (is (= [0 2 4] (take 3 s)))))

      (testing "items are passed through unchanged, metadata included"
        (let [a (with-meta [:a] {:k 1})
              b (with-meta [:b] {:k 2})
              res (filter (constantly true) [a b])]
          (is (= [[:a] [:b]] res))
          (is (identical? a (first res)))
          (is (identical? b (second res)))
          (is (= {:k 1} (meta (first res))))
          (is (= {:k 2} (meta (second res)))))))

    (testing "negative cases"
      ;; Note: the resulting lazy-seq must be realized to force the
      ;; exception
      (are [pred] (p/thrown? (doall (filter pred [1 2 3])))
        nil
        1
        "a"
        true
        \a)

      (are [coll] (p/thrown? (doall (filter even? coll)))
        1
        true
        false
        :a
        'a
        #?@(;; Chars are seqable in CLJS and Basilisp (1-char strings) but
            ;; not in the JVM implementations
            :cljs []
            :default [\a]))

      ;; `nil` is not a transducer either
      (is (p/thrown? (into [] (filter nil) [1 2 3])))
      (is (p/thrown? (transduce (filter nil) conj [1 2 3]))))))
