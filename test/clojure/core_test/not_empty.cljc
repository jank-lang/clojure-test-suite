(ns clojure.core-test.not-empty
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists not-empty
  (deftest test-not-empty
    (testing "common"
      (is (= nil (not-empty {})))
      (is (= nil (not-empty #{})))
      (is (= nil (not-empty [])))
      (is (= nil (not-empty '())))
      (is (= nil (not-empty "")))
      (is (= {:a :b} (not-empty {:a :b})))
      (is (= #{1 "a"} (not-empty #{1 "a"})))
      (is (= [\space] (not-empty [\space])))
      (is (= '(nil) (not-empty '(nil))))
      (is (= "abc" (not-empty "abc")))
      #?@(:cljs [(is (= "a" (not-empty \a)))
                 (is (thrown? js/Error (not-empty 0)))
                 (is (thrown? js/Error (not-empty 0.0)))]
          :default [(is (thrown? Exception (not-empty \a)))
                    (is (thrown? Exception (not-empty 0)))
                    (is (thrown? Exception (not-empty 0.0)))]))))
