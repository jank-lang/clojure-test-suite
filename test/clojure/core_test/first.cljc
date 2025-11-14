(ns clojure.core-test.first
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists first
  (deftest test-first
    (is (= 0 (first (range 0 10))))
    (is (= 0 (first (range))))
    (is (= :a (first [:a :b :c])))
    (is (= :a (first '(:a :b :c))))
    (is (nil? (first '())))
    (is (nil? (first [])))
    (is (nil? (first nil)))))
