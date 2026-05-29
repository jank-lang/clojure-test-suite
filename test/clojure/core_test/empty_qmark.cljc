(ns clojure.core-test.empty-qmark
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists empty?
  (deftest test-empty?
    (testing "common"
      (is (= true (empty? nil)))
      (is (= true (empty? {})))
      (is (= true (empty? [])))
      (is (= true (empty? "")))
      (is (= true (empty? '())))
      (is (= true (empty? #{})))
      (is (= false (empty? [\a])))
      (is (= false (empty? '(nil))))
      (is (= false (empty? (range))))
      (is (= false (empty? "abc")))
      (is (= false (empty? #{0 \space "a"})))
      (is (= false (empty? [(repeat (range))])))
      ;; Phel's `empty?` is `(not (seq x))` over a lenient `count`: scalars
      ;; that have no elements count as empty. `(empty? 0)` is `true` (zero
      ;; has no elements), while `0.0` and a space character report `false`.
      ;; Documented divergence.
      #?@(:phel [(is (= true (empty? 0)))
                 (is (= false (empty? 0.0)))
                 (is (= false (empty? \space)))]
          :lpy [(is (= false (empty? \space)))
                (is (p/thrown? (empty? 0)))
                (is (p/thrown? (empty? 0.0)))]
          :cljs [(is (= false (empty? \space)))
                 (is (p/thrown? (empty? 0)))
                 (is (p/thrown? (empty? 0.0)))]
          :default [(is (p/thrown? (empty? 0)))
                    (is (p/thrown? (empty? 0.0)))
                    (is (p/thrown? (empty? \space)))]))))
