(ns clojure.core-test.rand-nth
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists rand-nth
  (deftest test-rand-nth
    ;; Strategy: call `rand-nth` many times on a large collection and
    ;; verify that the results are not constant and are within range
    ;; of the items in the collection. Note that we do NOT validate
    ;; the quality of the randomness in any way.
    (testing "basic case"
      (let [draws 100
            nitems 10000
            coll (doall (range nitems)) ; just need unique items in the coll
            samples (repeatedly draws #(rand-nth coll))]
        (is (> (count (set samples)) 1)) ; unlikely to be constant
        (is (every? #(< -1 % nitems) samples)))) ; in-range?

    (testing "negative cases"
      (is (nil? (rand-nth nil)))
      (is (thrown? #?(:cljs :default :default Exception) (rand-nth 1))))))
