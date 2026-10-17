(ns clojure.core-test.with-meta
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists with-meta
  (deftest test-with-meta
    (testing "literals"
      (are [v]
           (-> v (with-meta {:foo true}) meta :foo)
        {}
        {:a :b :c :d}
        []
        [0 1 2]
        #{}
        #{:a :b :c}
        '()
        '(:a :b :c)
        'foo
        #()))

    (testing "constructed values"
      (are [v]
           (-> v (with-meta {:foo true}) meta :foo)
        (hash-map)
        (vector)
        (hash-set)
        (list)
        (symbol "foo")
        (fn [])
        (keys {:a :b :c :d}) ;; KeySeq
        (vals {:a :b :c :d}) ;; ValSeq
        (subvec [1 2] 0 1) ;; SubVector
        (concat [1 2 3]) ;; LazySeq
        (range 10) ;; LongRange
        (range 10.0) ;; Range
        ((fn [& args] args) 1 2 3) ;; ArraySeq
        (repeat 5 0) ;; Repeat
        (cons 1 []) ;; Cons
        ))

    (testing "seq values"
      (are [v]
           (-> v seq (with-meta {:foo true}) meta :foo)
        {:a :b :c :d}
        [0 1 2]
        #{:a :b :c}
        '(:a :b :c)
        "foo"))

    (testing "ensure existing meta is replaced"
      (are [v]
           (= {:foo false} (-> v (with-meta {:foo false}) meta))
        ^{:foo true :bar true} {}
        ^{:foo true :bar true} []
        ^{:foo true :bar true} #{}))

    (testing "invalid values"
      (are [v]
           (p/thrown? (with-meta v {:foo true}))
        true
        false
        3.14
        42
        "foo"
        :foo
        (keyword "foo")))

    (when-var-exists sorted-map
      (testing "sorted map"
        (is (-> (sorted-map) (with-meta {:foo true}) meta :foo))
        (is (-> (sorted-map :a :b) seq (with-meta {:foo true}) meta :foo))))

    (when-var-exists sorted-set
      (testing "sorted set"
        (is (-> (sorted-set) (with-meta {:foo true}) meta :foo))
        (is (-> (sorted-set :a :b) seq (with-meta {:foo true}) meta :foo))))))

