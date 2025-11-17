(ns clojure.core-test.find
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists find
  (deftest test-find
    (testing "common"
      (is (= nil (find nil nil)))
      (is (= nil (find {} nil)))
      (is (= nil (find [] nil)))
      (is (= [:a 1] (find {:a 1 :b 2 :c 3} :a)))
      (is (= [:a 1] (find {:a 1 :b 2 :c (range)} :a)))
      (is (= nil (find {:a 1 :b 2 :c 3} :d)))
      (is (= [0 1] (find {0 1 :0 2 "0" 3} 0))))))
