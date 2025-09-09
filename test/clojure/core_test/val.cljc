(ns clojure.core-test.val
  (:require [clojure.test :as t :refer [deftest testing is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists val
  (deftest test-val
    (testing "`val` on map-entry-like things"
      (is (= :v (val (first {:k :v}))))
      (is (= :v (val (first {:k :v, :one :two}))))
      ;; Note: the following may be built on shaky ground, per Rich:
      ;; https://groups.google.com/g/clojure/c/FVcrbHJpCW4/m/Fh7NsX_Yb7sJ
      (is (= 'v (val #?(:cljs    (cljs.core/MapEntry. 'k 'v nil)
                        :default (clojure.lang.MapEntry/create 'k 'v)))))
      (is (= :b (val (first (sorted-map :a :b)))))
      (is (= :b (val (first (hash-map :a :b)))))
      (is (= :b (val (first (array-map :a :b))))))
    (testing "`val` throws on lots of things"
      #?@(:cljs    [(is (thrown? js/Error (val nil)))
                    (is (thrown? js/Error (val 0)))
                    (is (thrown? js/Error (val '())))
                    (is (thrown? js/Error (val '(1 2))))
                    (is (thrown? js/Error (val {})))
                    (is (thrown? js/Error (val {1 2})))
                    (is (thrown? js/Error (val [])))
                    (is (thrown? js/Error (val [1 2])))
                    (is (thrown? js/Error (val #{})))
                    (is (thrown? js/Error (val #{1 2})))]
          :default [(is (thrown? Exception (val nil)))
                    (is (thrown? Exception (val 0)))
                    (is (thrown? Exception (val '())))
                    (is (thrown? Exception (val '(1 2))))
                    (is (thrown? Exception (val {})))
                    (is (thrown? Exception (val {1 2})))
                    (is (thrown? Exception (val [])))
                    (is (thrown? Exception (val [1 2])))
                    (is (thrown? Exception (val #{})))
                    (is (thrown? Exception (val #{1 2})))]))))
