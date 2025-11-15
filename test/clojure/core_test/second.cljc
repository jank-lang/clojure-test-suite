(ns clojure.core-test.second
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists second
  (deftest test-second
    (is (= 1 (second (range 0 10))))
    (is (= 1 (second (range))))
    (is (= :b (second [:a :b :c])))
    (is (= :b (second '(:a :b :c))))
    (is (nil? (second '())))
    (is (nil? (second [])))
    (is (nil? (second nil)))))
