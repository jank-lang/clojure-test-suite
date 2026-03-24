(ns clojure.core-test.last
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists last
  (deftest test-last
    (testing "common"
      (is (= 9 (last (range 0 10))))
      (is (= :c (last [:a :b :c])))
      (is (= :c (last '(:a :b :c))))
      (is (= \d (last "abcd")))
      (is (= \a (last "a")))
      (is (= nil (last '())))
      (is (= nil (last [])))
      (is (= nil (last #{})))
      (is (= nil (last nil))))

    (testing "exceptions"
      #?@(:cljs
          [(is (p/thrown? (last 0)))]
          :lpy
          [(is (= \a (last \a)))
           (is (p/thrown? (last 0)))]
          :default
          [(is (p/thrown? (last \a)))
           (is (p/thrown? (last 0)))]))))
