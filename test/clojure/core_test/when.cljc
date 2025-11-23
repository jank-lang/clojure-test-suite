(ns clojure.core-test.when
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when
  (deftest test-when
    (testing "`when` checks logical truth"
      (is (nil? (when nil :foo)))
      (is (nil? (when false :foo)))
      (is (nil? (when ((constantly nil)) :foo)))

      (testing "without a body, truth doesn't matter"
        (is (nil? (when false)))
        (is (nil? (when true))))

      (testing "things which are false in other languages but not false in Clojure"
        (is (= :foo (when 0 :foo)))
        (is (= :foo (when "" :foo)))
        (is (= :foo (when (list) :foo)))
        (is (= :foo (when '() :foo))))

      (is (= :foo (when true :foo)))
      (is (= :foo (when (constantly nil) :foo)))
      (is (= :foo (when "false" :foo)))
      (is (= :foo (when [] :foo)))
      (is (= :foo (when {} :foo)))
      (is (= :foo (when #{} :foo)))
      (is (= :foo (when :haberdashery :foo))))

    (testing "`when` has implicit `do`"
      (is (= :bar
             (when true :foo :bar)))
      (let [foo (atom 0)]
        (is (= :bar (when true
                      (swap! foo inc)
                      (swap! foo inc)
                      (swap! foo inc)
                      :bar)))
        (is (= 3 @foo))))))
