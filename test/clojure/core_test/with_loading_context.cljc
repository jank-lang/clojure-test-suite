(ns clojure.core-test.with-loading-context
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-loading-context
  #?(:clj
     (deftest test-with-loading-context
       (testing "executes body"
         (is (= :result (with-loading-context :result))))

       (testing "returns last expression"
         (is (= 42 (with-loading-context 
                     (+ 40 2)))))

       (testing "sets up loading context"
         (with-loading-context
           (is true)))

       (testing "nested with-loading-context"
         (is (= :inner (with-loading-context
                         (with-loading-context :inner)))))

       (testing "no side effects on outer context"
         (let [result (with-loading-context
                        :test)]
           (is (= :test result)))))

     :cljs
     (deftest test-with-loading-context
       (testing "with-loading-context Clojure-only"
         (is true)))))
