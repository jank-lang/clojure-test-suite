(ns clojure.core-test.hash-map
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists hash-map
  (deftest test-hash-map
    (testing "common"
      (is (= {} (hash-map)))
      (is (= {:a 1} (hash-map :a 1)))
      (is (= {:a 2} (hash-map :a 1 :a 2)))
      (is (= {:a 1 :b "2"} (hash-map :a 1, :b "2")))
      (is (= {"a" 1, [:b :c] "2", \d nil} (hash-map "a" 1, [:b :c] "2", \d nil)))
      (is (= {\a {}} (hash-map \a (hash-map))))
      (is (= {:a {:b {:c 1} :d 2}} (hash-map :a (hash-map :b (hash-map :c 1) :d 2))))
      #?@(:cljs [(is (thrown? js/Error (hash-map :a)))]
          :default [(is (thrown? Exception (hash-map :a)))]))))
