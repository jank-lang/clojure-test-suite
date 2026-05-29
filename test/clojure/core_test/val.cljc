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
      ;; Phel divergence: `val` is lenient — it calls `next` on its arg and
      ;; returns the second element (or nil) instead of throwing on non-MapEntry
      ;; inputs. Only `0` (a non-seqable scalar) throws.
      #?(:phel
         (do
           (is (p/thrown? (val 0)))
           (is (nil? (val nil)))
           (is (nil? (val '())))
           (is (= 2 (val '(1 2))))
           (is (nil? (val {})))
           (is (nil? (val {1 2})))
           (is (nil? (val [])))
           (is (= 2 (val [1 2])))
           (is (nil? (val #{})))
           (is (= 2 (val #{1 2}))))
         :default
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
           #{1 2})))))
