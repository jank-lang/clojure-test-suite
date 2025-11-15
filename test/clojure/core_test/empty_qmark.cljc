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
                 (is (thrown? js/Error (empty? 0)))
                 (is (thrown? js/Error (empty? 0.0)))]
          :default [(is (thrown? Exception (empty? 0)))
                    (is (thrown? Exception (empty? 0.0)))
                    (is (thrown? Exception (empty? \space)))]))))
