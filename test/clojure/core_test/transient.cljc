(ns clojure.core-test.transient
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists transient
  (deftest test-transient
    (testing "creation"
      (are [coll] (let [a-transient (transient coll)]
                    (= coll (persistent! a-transient)))
                  [1 2 3]
                  {:x 0 :y -1}
                  #{42 "life"}))
    
    (testing "support read-only interface"
      (testing "for transient vector"
        (let [avec [1 2 3]]
          (is (= (nth avec 1) (nth (transient avec) 1)))
          (is (= (get avec 1) (get (transient avec) 1)))
          (is (= (contains? avec 1) (contains? (transient avec) 1)))
          (is (= (avec 1) ((transient avec) 1)))
          (is (= (count avec) (count (transient avec))))))
      
      (testing "for transient map"
        (let [amap {:x 1 :y -1}]
          (is (= (get amap :x) (get (transient amap) :x)))
          (is (= (contains? amap :x) (contains? (transient amap) :x)))
          (is (= (:x amap) (:x (transient amap))))
          (is (= (amap :x) ((transient amap) :x)))
          (is (= (count amap) (count (transient amap))))))
      
      (testing "for transient set"
        (let [someset #{42 "life"}]
          (is (= (get someset 42) (get (transient someset) 42))) 
          (is (= (contains? someset 42) (contains? (transient someset) 42)))
          (is (= (someset 42) ((transient someset) 42)))
          (is (= (count someset) (count (transient someset)))))))
    
    (testing "calling non-bang interface throws"
      (testing "for transient vector"
        (let [avec [1 2 3]]
          (is (thrown? #?(:cljs js/Error :default Exception) (assoc (transient avec) 0 5)))
          (is (thrown? #?(:cljs js/Error :default Exception) (conj (transient avec) 5)))
          (is (thrown? #?(:cljs js/Error :default Exception) (pop (transient avec))))))
      
      (testing "for transient map"
        (let [amap {:x 1 :y -1}]
          (is (thrown? #?(:cljs js/Error :default Exception) (assoc (transient amap) :x 5)))
          (is (thrown? #?(:cljs js/Error :default Exception) (dissoc (transient amap) :x)))
          (is (thrown? #?(:cljs js/Error :default Exception) (conj (transient amap) [:x 5])))))
      
      (testing "for transient set"
        (let [someset #{42 "life"}]
          (is (thrown? #?(:cljs js/Error :default Exception) (disj (transient someset) 42)))
          (is (thrown? #?(:cljs js/Error :default Exception) (conj (transient someset) 43))))))
    
    (testing "calling transient a second time throws"
      (are [a-transient] (thrown? #?(:cljs js/Error :default Exception) (transient a-transient))
                         (transient [1 2 3])
                         (transient {:x 1 :y -1})
                         (transient #{42 "life"}))) 
    
    (testing "bad input"
      (are [v] (thrown? #?(:cljs js/Error :default Exception) (transient v))
               'sym
               `sym
               "meow"
               1
               1.0
               #?@(:cljs [] ; most Clojure dialects support ratios - not CLJS
                   :default [111/7])
               \newline
               nil
               true
               false
               ##Inf
               :kw
               :ns/kw
               #(+ 1 %)
               '(1 2 3)))))
