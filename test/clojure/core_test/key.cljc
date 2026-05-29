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
      ;; Phel's `key` is intentionally lenient: instead of throwing on
      ;; non-map-entry input it returns `nil` for empty/non-pair collections
      ;; and the first element for sequential pairs (so `(key {1 2})` yields
      ;; the `[1 2]` entry vector, `(key [1 2])`/`(key #{1 2})` yields `1`).
      ;; Documented divergence.
      #?(:phel (do
                 (is (nil? (key nil)))
                 (is (nil? (key 0)))
                 (is (nil? (key '())))
                 (is (= 1 (key '(1 2))))
                 (is (nil? (key {})))
                 (is (= [1 2] (key {1 2})))
                 (is (nil? (key [])))
                 (is (= 1 (key [1 2])))
                 (is (nil? (key #{})))
                 (is (= 1 (key #{1 2}))))
         :default (are [arg] (p/thrown? (key arg))
                    nil
                    0
                    '()
                    '(1 2)
                    {}
                    {1 2}
                    []
                    [1 2]
                    #{}
                    #{1 2})))))
