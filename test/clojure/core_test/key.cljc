(ns clojure.core-test.key
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists key
  (deftest test-key
    (testing "basic tests"
      (is (= nil (key (first {nil nil}))))
      (is (= :k (key (first {:k :v}))))
      (is (= :k (key (first (hash-map :k :v)))))
      (when-var-exists sorted-map
        (is (= :k (key (first (sorted-map :k :v))))))
      (when-var-exists array-map
        (is (= :k (key (first (array-map :k :v)))))))
    (testing "`key` throws on lots of things"
      (are [arg] (p/thrown? (key arg))
        nil
        0
        '()
        '(1 2)
        {}
        {1 2}
        []
        [1 2]
        #{}
        #{1 2}))))
