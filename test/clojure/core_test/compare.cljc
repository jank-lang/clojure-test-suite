(ns clojure.core-test.compare
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists compare
 (deftest test-compare
   (testing "numeric-types"
     (are [pred args] (pred (compare (first args) (second args)))
       neg?   [0  10]
       zero?  [0  0]
       pos?   [0 -100N]
       zero?  [1  1.0]
       neg?   [0  0x01]
       neg?   [0  2r01]
       pos?   [1  nil]
       #?@(:cljs []
           :default
           [neg? [1 100/3]]))

     (is (thrown? #?(:cljs :default :default Exception) (compare 1 []))))

  (testing "lexical-types"
    (are [pred args] (pred (compare (first args) (second args)))
      neg?  [\a    \b]
      zero? [\0    \0]
      pos?  [\z    \a]
      neg?  ["cat" "dog"]
      neg?  ['cat  'dog]
      neg?  [:cat  :dog]
      zero? [:dog  :dog]
      neg?  [:cat  :animal/cat]
      pos?  ['a    nil])

    (is (thrown? #?(:cljs :default :default Exception) (compare "a" [])))
    (is (thrown? #?(:cljs :default :default Exception) (compare "cat" '(\c \a \t)))))

  (testing "collection-types"
    (are [pred args] (pred (compare (first args) (second args)))
      zero?  [[]          []]
      pos?   [[3]         [1]]
      neg?   [[]          [1 2]]
      neg?   [[]          [[]]]
      pos?   [[]          nil]
      ;; Sets, maps, and lists don't implement java.lang.Comparable,
      ;; so just comment these out for now. TODO: make decision as to
      ;; what we want to do with these tests.
      ;; zero?  [#{}         #{}]
      ;; zero?  [{}          {}]
      ;; zero?  [(array-map) (array-map)]
      ;; zero?  [(hash-map)  (hash-map)]
      ;; zero?  [{}          (hash-map)]
      ;; zero?  [{}          (array-map)]
      ;; zero?  ['()         '()]
      )

    (is (thrown? #?(:cljs :default :default Exception) (compare []  '())))
    (is (thrown? #?(:cljs :default :default Exception) (compare [1] [[]])))
    (is (thrown? #?(:cljs :default :default Exception) (compare []  {})))
    (is (thrown? #?(:cljs :default :default Exception) (compare []  #{})))
    (is (thrown? #?(:cljs :default :default Exception) (compare #{} (sorted-set))))
    (is (thrown? #?(:cljs :default :default Exception) (compare #{1} #{1})))
    (is (thrown? #?(:cljs :default :default Exception) (compare {1 2} {1 2})))
    (is (thrown? #?(:cljs :default :default Exception) (compare (range 5) (range 5))))
    ;; Clojurescript goes into an infinite loop of some sort when compiling this.
    #_(is (thrown? #?(:cljs :default :default Exception) (compare (range 5) (range)))))))
