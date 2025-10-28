(ns clojure.core-test.with-meta
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-meta
  (deftest test-basic-metadata
    (testing "adds metadata to vector"
      (let [v (with-meta [1 2 3] {:a 1})]
        (is (= (meta v) {:a 1}))
        (is (= v [1 2 3]))))
    
    (testing "adds metadata to list"
      (let [l (with-meta '(1 2 3) {:b 2})]
        (is (= (meta l) {:b 2}))
        (is (= l '(1 2 3)))))
    
    (testing "adds metadata to map"
      (let [m (with-meta {:x 1} {:c 3})]
        (is (= (meta m) {:c 3}))
        (is (= m {:x 1}))))
    
    (testing "adds metadata to set"
      (let [s (with-meta #{1 2 3} {:d 4})]
        (is (= (meta s) {:d 4}))
        (is (= s #{1 2 3}))))
    
    (testing "adds metadata to symbol"
      (let [sym (with-meta 'foo {:e 5})]
        (is (= (meta sym) {:e 5}))
        (is (= sym 'foo)))))
  
  (deftest test-nil-handling
    (testing "nil metadata"
      (let [v (with-meta [1 2] nil)]
        (is (nil? (meta v)))
        (is (= v [1 2])))))
  
  (deftest test-metadata-replacement
    (testing "replaces existing metadata"
      (let [v (with-meta [1 2] {:a 1})
            v2 (with-meta v {:b 2})]
        (is (= (meta v2) {:b 2}))
        (is (= (meta v) {:a 1}))))
    
    (testing "does not modify original"
      (let [v1 (with-meta [1 2] {:a 1})
            v2 (with-meta v1 {:b 2})]
        (is (= (meta v1) {:a 1}))
        (is (= (meta v2) {:b 2})))))
  
  (deftest test-metadata-types
    (testing "metadata with various value types"
      (let [m {:string "value"
               :number 42
               :bool true
               :vector [1 2]
               :map {:nested 1}
               :keyword :key}
            v (with-meta [1] m)]
        (is (= (meta v) m))))
    
    (testing "empty metadata map"
      (let [v (with-meta [1 2] {})]
        (is (= (meta v) {})))))
  
  (deftest test-equality
    (testing "objects with different metadata are equal"
      (let [v1 (with-meta [1 2] {:a 1})
            v2 (with-meta [1 2] {:b 2})]
        (is (= v1 v2))))
    
    (testing "objects with and without metadata are equal"
      (let [v1 [1 2]
            v2 (with-meta [1 2] {:a 1})]
        (is (= v1 v2)))))
  
  (deftest test-common-metadata-keys
    (testing "type metadata"
      (let [v (with-meta [1] {:type :custom})]
        (is (= (:type (meta v)) :custom))))
    
    (testing "tag metadata"
      (let [v (with-meta [1] {:tag String})]
        (is (= (:tag (meta v)) String))))
    
    (testing "doc metadata"
      (let [v (with-meta 'foo {:doc "documentation"})]
        (is (= (:doc (meta v)) "documentation"))))
    
    (testing "private metadata"
      (let [v (with-meta 'bar {:private true})]
        (is (= (:private (meta v)) true)))))
  
  (deftest test-structural-sharing
    (testing "maintains structural equality"
      (let [original [1 2 3]
            with-m (with-meta original {:a 1})]
        (is (= original with-m))
        (is (= (count original) (count with-m)))
        (is (= (first original) (first with-m))))))
  
  (deftest test-chaining
    (testing "can chain with-meta calls"
      (let [v [1 2]
            v1 (with-meta v {:a 1})
            v2 (with-meta v1 {:b 2})
            v3 (with-meta v2 {:c 3})]
        (is (= (meta v3) {:c 3}))
        (is (= v3 [1 2])))))
  
  (deftest test-nested-structures
    (testing "metadata only on outer structure"
      (let [inner [1 2]
            outer (with-meta [inner] {:a 1})]
        (is (= (meta outer) {:a 1}))
        (is (nil? (meta (first outer))))))
    
    (testing "metadata on nested structures"
      (let [inner (with-meta [1 2] {:inner true})
            outer (with-meta [inner] {:outer true})]
        (is (= (meta outer) {:outer true}))
        (is (= (meta (first outer)) {:inner true}))))))
