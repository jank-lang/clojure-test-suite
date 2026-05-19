(ns clojure.core-test.val
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists val
  (deftest test-val
    (testing "basic tests"
      (is (nil? (val (first {nil nil}))))
      (is (= :v (val (first {:k :v}))))
      (is (= :v (val (first (hash-map :k :v)))))
      (when-var-exists sorted-map
        (is (= :v (val (first (sorted-map :k :v))))))
      (when-var-exists array-map
        (is (= :v (val (first (array-map :k :v)))))))
    (testing "`val` throws on lots of things"
      (are [arg] (p/thrown? (val arg))
        nil
        0
        '()
        '(1 2)
        {}
        {1 2}
        []
        [1 2]                           ; might be dialect-specific
        #{}
        #{1 2}))))
