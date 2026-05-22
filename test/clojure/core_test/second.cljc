(ns clojure.core-test.second
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists second
  (deftest test-second
    (is (= 1 (second (range 0 10))))
    (is (= 1 (second (range))))
    ;; Sorted collections not currently implemented in Basilisp
    #?(:lpy nil :default (is (= 1 (second (sorted-set 0 1 2)))))
    #?(:lpy nil :default (is (= [:b 1] (second (sorted-map :a 0 :b 1 :c 2)))))
    (is (= :b (second [:a :b :c])))
    (is (= :b (second '(:a :b :c))))
    (is (nil? (second '())))
    (is (nil? (second [])))
    (is (nil? (second nil)))))
