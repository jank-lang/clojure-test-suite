(ns clojure.core-test.empty-qmark
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

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
      #?@(:cljs [(is (= false (empty? \space)))
                 (is (p/thrown? (empty? 0)))
                 (is (p/thrown? (empty? 0.0)))]
          :lpy [(is (= false (empty? \space)))
                (is (p/thrown? (empty? 0)))
                (is (p/thrown? (empty? 0.0)))]
          :default [(is (p/thrown? (empty? 0)))
                    (is (p/thrown? (empty? 0.0)))
                    (is (p/thrown? (empty? \space)))]))))
