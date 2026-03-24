(ns clojure.core-test.repeat
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists repeat
  (deftest test-repeat

    (testing "repeat x"
      (are [n x expected] (= (take n (repeat x)) expected)
                          1 :a [:a]
                          2 [:a] [[:a] [:a]]
                          3 "a" ["a" "a" "a"]
                          7 nil [nil nil nil nil nil nil nil]))

    (testing "repeat n x"
      (are [n x expected] (= (repeat n x) expected)
                          -1 :a []
                          0 :a []
                          1 :a [:a]
                          3 :a [:a :a :a]
                          3.14 :a #?(:cljs    [:a :a :a :a]
                                     :default [:a :a :a])
                          3.99 :a #?(:cljs    [:a :a :a :a]
                                     :default [:a :a :a])
                          7 :a [:a :a :a :a :a :a :a]
                          7 nil [nil nil nil nil nil nil nil]))

    (testing "bad shape"

      (testing "n not being a number"
        (are [n x] #?(:cljs    (= [] (repeat n x))
                      :default (p/thrown? (repeat n x)))
                   nil nil
                   "a" :a
                   :a :a)

        (testing "n being a boolean"
          (is #?(:clj     (p/thrown? (repeat true :a))
                 :default (= [:a] (repeat true :a))))
          (is #?(:clj     (p/thrown? (repeat false :a))
                 :default (= [] (repeat false :a)))))))))
