(ns clojure.core-test.fnext
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists fnext
  (deftest test-fnext
    (testing "section name"
      (is (= nil (fnext '())))
      (is (= nil (fnext [])))
      (is (= nil (fnext {})))
      (is (= nil (fnext #{})))
      (is (= nil (fnext nil)))
      (is (= nil (fnext "")))
      (is (= nil (fnext "a")))
      (is (= nil (fnext {:a :b})))
      (is (= :b (fnext [:a :b])))
      (is (= 1 (fnext (range 0 10))))
      (is (= 1 (fnext (range)))) ; infinite lazy seq
      (is (= [2 3] (fnext [[0 1] [2 3]])))
      (is (= '(2 3) (fnext '([0 1] [2 3]))))
      (is (= \b (fnext "abcd")))
      (is (= "cd" (fnext ["ab" "cd"])))
      (is (= nil (fnext ["abcd"])))
      (is (= nil (fnext #{"abcd"}))))
    
    (testing "exceptions"
      #?@(:cljs
          [(is (thrown? js/Error (fnext 0)))]
          :default
          [(is (thrown? Exception (fnext 0)))
           (is (thrown? Exception (fnext \a )))]))))
