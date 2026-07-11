(ns clojure.core-test.first
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists first
  (deftest test-first
    (is (= 0 (first (range 0 10))))
    (is (= 0 (first (range))))          ; infinite lazy range
    ;; Sorted collections not currently implemented in Basilisp
    #?(:lpy nil :default (is (= 0 (first (sorted-set 0 1 2)))))
    #?(:lpy nil :default (is (= [:a 0] (first (sorted-map :a 0 :b 1 :c 2)))))
    ;; Try lots of different types that can be turned into seqs
    (is (= :a (first [:a :b :c])))
    (is (= :a (first '(:a :b :c))))
    (is (= :a (first (to-array [:a :b :c]))))
    (is (= 1 (first (int-array [1 2 3]))))
    (is (= [:a 1] (first {:a 1})))      ; Map is not ordered, so one item
    (is (= :a (first #{:a})))           ; Set is not ordered, so one item
    (is (= \h (first "hello")))
    (is (nil? (first '())))
    (is (nil? (first [])))
    (is (nil? (first {})))
    (is (nil? (first #{})))
    (is (nil? (first "")))
    (is (nil? (first nil)))))
