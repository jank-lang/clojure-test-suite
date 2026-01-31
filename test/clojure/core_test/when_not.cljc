(ns clojure.core-test.when-not
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-not
  (deftest test-when-not
    (testing "`when-not` checks logical falsity"
      (is (= :foo (when-not nil :foo)))
      (is (= :foo (when-not false :foo)))
      (is (= :foo (when-not ((constantly nil)) :foo)))

      (testing "without a body, falsity doesn't matter"
        (is (nil? (when-not false)))
        (is (nil? (when-not true))))

      (testing "things which are false in other languages but truthy in Clojure"
        (is (nil? (when-not 0 :foo)))
        (is (nil? (when-not "" :foo)))
        (is (nil? (when-not (list) :foo)))
        (is (nil? (when-not '() :foo))))

      (is (nil? (when-not true :foo)))
      (is (nil? (when-not (constantly nil) :foo)))
      (is (nil? (when-not "false" :foo)))
      (is (nil? (when-not [] :foo)))
      (is (nil? (when-not {} :foo)))
      (is (nil? (when-not #{} :foo)))
      (is (nil? (when-not :haberdashery :foo))))

    (testing "`when-not` has implicit `do`"
      (is (= :bar
             (when-not false :foo :bar)))
      (let [foo (atom 0)]
        (is (= :bar (when-not false
                      (swap! foo inc)
                      (swap! foo inc)
                      (swap! foo inc)
                      :bar)))
        (is (= 3 @foo))))))
