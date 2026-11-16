(ns clojure.core-test.meta
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/meta

  ;; DOCSTRING:
  ;; clojure.core/meta
  ;; [obj]
  ;; Returns the metadata of obj, returns nil if there is no metadata.

  (deftest test-meta
    (testing "values without metadata"
      (are [obj] (nil? (meta obj))
        nil
        true
        false
        0
        42
        -1.5
        "foo"
        \a
        :foo
        'foo
        []
        {}
        #{}
        [1 2 3]
        {:a 1}
        #{:a :b}
        ;; a list constructed at run time has no reader metadata
        (list)
        (list 1 2 3)))

    (testing "values with metadata"
      (are [obj] (= {:a 1} (meta (with-meta obj {:a 1})))
        []
        [0 1 2]
        {}
        {:a :b}
        #{}
        #{:a :b}
        (list)
        (list 1 2 3)
        'foo
        (symbol "foo")
        (fn [])
        (range 3)
        (lazy-seq nil)))

    (testing "empty metadata is metadata, not nil"
      (is (= {} (meta (with-meta [] {}))))
      (is (= {} (meta (with-meta (list) {})))))

    (testing "metadata attached by the reader"
      (is (= {:a 1} (meta ^{:a 1} [])))
      (is (= {:a 1} (meta ^{:a 1} {:b 2}))))

    (testing "vars carry the metadata that def attaches"
      (is (= 'inc (:name (meta #'inc)))))))
