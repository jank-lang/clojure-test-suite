(ns clojure.core-test.shuffle
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists shuffle
  (deftest test-shuffle
    (testing "different collection types"
      (let [x [1 2 3]
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      (let [x #{1 2 3}
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      (let [x '(1 2 3)
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      #?(:cljs
         (let [x "abc"
               actual (shuffle x)]
           (is (vector? actual))
           (is (= (count x) (count actual))))))
    (testing "negative cases"
      #?(:cljs (is (p/thrown? (shuffle 1)))
         :default (is (p/thrown? (shuffle 1))))
      #?@(:cljs
          [(is (= [] (shuffle nil)))
           (is (= [] (shuffle {})))]
          :cljr
          [(is (p/thrown? (shuffle nil)))
           (is (p/thrown? (shuffle "abc")))
           (is (= [] (shuffle {})))]
          :lpy
          [(is (p/thrown? (shuffle nil)))
           (is (= #{"a" "b" "c"} (set (shuffle "abc"))))
           (is (= [] (shuffle {})))]
          :default
          [(is (p/thrown? (shuffle nil)))
           (is (p/thrown? (shuffle "abc")))
           (is (p/thrown? (shuffle {})))]))))
