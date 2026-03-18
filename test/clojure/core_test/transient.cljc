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
        (are [op] (let [avec [1 2 3]]
                    (= (op avec) (op (transient avec))))
                  #(nth % 1)
                  #(get % 1)
                  #(contains? % 1)
                  #(% 1)
                  count))
      
      (testing "for transient map"
        (are [op] (let [amap {:x 1 :y -1}]
                    (= (op amap) (op (transient amap))))
                  #(get % :x)
                  #(contains? % :x)
                  #(:x %)
                  #(% :x)
                  count))
      
      (testing "for transient set"
        (are [op] (let [aset {42 "life"}]
                    (= (op aset) (op (transient aset))))
                  #(get % 42)
                  #(contains? % 42)
                  #(% 42)
                  count)))
    
    (testing "calling non-bang interface on transient throws"
      (testing "for transient vector"
        (are [op] (thrown? #?(:cljs js/Error :default Exception) (op (transient [1 2 3])))
                  #(assoc % 0 5)
                  #(conj % 5)
                  #(pop %)))
      
      (testing "for transient map"
        (are [op] (thrown? #?(:cljs js/Error :default Exception) (op (transient {:x 1 :y -1})))
                  #(assoc % :x 5)
                  #(dissoc % :x)
                  #(conj % [:x 5])))
      
      (testing "for transient set"
        (are [op] (thrown? #?(:cljs js/Error :default Exception) (op (transient {:x 1 :y -1})))
                  #(disj % 42)
                  #(conj % 42))))
    
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
