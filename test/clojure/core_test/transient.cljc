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
               #?(:cljs "cljs is the only (?) Clojure dialect that doesn't support ratios"
                  :default 111/7)
               \newline
               nil
               true
               false
               ##Inf
               :kw
               :ns/kw
               '(1 2 3)))))
