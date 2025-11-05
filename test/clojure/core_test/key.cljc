(ns clojure.core-test.key
  (:require [clojure.test :as t :refer [deftest testing is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists key
  (deftest test-key
    (testing "`key` on map-entry-like things"
      (is (= nil (key (first {nil nil}))))
      (is (= :k (key (first {:k :v}))))
      (is (= :k (key (first {:k :v, :one :two}))))
      ;; Note: the following may be built on shaky ground, per Rich:
      ;; https://groups.google.com/g/clojure/c/FVcrbHJpCW4/m/Fh7NsX_Yb7sJ
      (is (= 'k (key #?(:cljs    (cljs.core/MapEntry. 'k 'v nil)
                        :default (clojure.lang.MapEntry/create 'k 'v)))))
      (is (= :a (key (first (sorted-map :a :b)))))
      (is (= :a (key (first (hash-map :a :b)))))
      (is (= :a (key (first (array-map :a :b))))))
    (testing "`key` throws on lots of things"
      #?@(:cljs    [(is (thrown? js/Error (key nil)))
                    (is (thrown? js/Error (key 0)))
                    (is (thrown? js/Error (key '())))
                    (is (thrown? js/Error (key '(1 2))))
                    (is (thrown? js/Error (key {})))
                    (is (thrown? js/Error (key {1 2})))
                    (is (thrown? js/Error (key [])))
                    (is (thrown? js/Error (key [1 2])))
                    (is (thrown? js/Error (key #{})))
                    (is (thrown? js/Error (key #{1 2})))]
          :default [(is (thrown? Exception (key nil)))
                    (is (thrown? Exception (key 0)))
                    (is (thrown? Exception (key '())))
                    (is (thrown? Exception (key '(1 2))))
                    (is (thrown? Exception (key {})))
                    (is (thrown? Exception (key {1 2})))
                    (is (thrown? Exception (key [])))
                    (is (thrown? Exception (key [1 2])))
                    (is (thrown? Exception (key #{})))
                    (is (thrown? Exception (key #{1 2})))]))))
