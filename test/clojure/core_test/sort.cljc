(ns clojure.core-test.sort
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists sort
  (deftest test-sort
    (testing "common cases -- unary"
      (are [expected x] (= expected (sort x))
        '() nil
        '(1 2 3 4) [3 1 2 4]
        '(nil 2 3 4) [3 nil 2 4]
        '(1 2 3 4) '(3 1 2 4)
        '(1 2 3 4) #{3 1 2 4}
        '([:a 1] [:b 2] [:c 3]) {:c 3 :b 2 :a 1}
        '([1] [2] [3] [4]) [[3] [1] [2] [4]]
        '("a" "b" "c" "d") ["b" "a" "c" "d"]
        '(\c \e \j \l \o \r \u) "clojure"))
    (testing "common cases -- binary"
      (is (= '(4 3 2 1) (sort #(- (compare %1 %2)) [1 2 3 4]))))
    (testing "negative cases"
      (is (thrown? #?(:cljs :default, :default Exception) (sort 1)))
      (is (thrown? #?(:cljs :default, :default Exception) (sort [1 []]))))
    (testing "stable sort"
      (is (= '([1 :a] [1 :e] [2 :b] [2 :d] [3 :c])
             (sort (fn [[a _] [b _]] (compare a b))
                   [[1 :a] [2 :b] [3 :c] [2 :d] [1 :e]]))))))
