(ns clojure.core-test.identity
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(defn identity-identical? [x]
  (identical? x (identity x)))

(when-var-exists identity
  (deftest test-identity
    (testing "Basic Identities"
      (are [x] (identity-identical? x)
        nil
        true
        false
        ""
        "foo"
        \a
        :foo
        :foo.bar/baz
        'foo
        'foo.bar/baz
        0
        1
        0.1
        -0.1
        ##Inf
        ##-Inf
        []
        '()
        #uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6"
        #inst "2010-11-12T13:14:15.666-05:00"
        (atom nil)))

    (testing "NaN"
      (is (NaN? (identity ##NaN))))

    (testing "Meta Preservation"
      (is (true? (:foo (meta (identity ^:foo {}))))))

    (testing "Lazy Infinite Sequence"
      (let [infinite-seq (map inc (range))]
        (is (not (realized? (identity infinite-seq))))))))
