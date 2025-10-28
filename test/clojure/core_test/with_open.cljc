(ns clojure.core-test.with-open
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-open
  #?(:clj
     (deftest test-with-open
       #?@(:bb []
           :default
           [(testing "closes resource after use"
              (let [closed (atom false)
                    resource (proxy [java.io.Reader] []
                               (close [] (reset! closed true))
                               (read ([] -1)))]
                (with-open [r resource]
                  (is (false? @closed)))
                (is (true? @closed))))

            (testing "closes on exception"
              (let [closed (atom false)
                    resource (proxy [java.io.Reader] []
                               (close [] (reset! closed true))
                               (read ([] -1)))]
                (try
                  (with-open [r resource]
                    (throw (Exception. "test")))
                  (catch Exception _))
                (is (true? @closed))))

            (testing "multiple resources"
              (let [closed1 (atom false)
                    closed2 (atom false)
                    r1 (proxy [java.io.Reader] []
                         (close [] (reset! closed1 true))
                         (read ([] -1)))
                    r2 (proxy [java.io.Reader] []
                         (close [] (reset! closed2 true))
                         (read ([] -1)))]
                (with-open [x r1 y r2]
                  :body)
                (is (true? @closed1))
                (is (true? @closed2))))])

       (testing "returns last expression"
         (let [result (with-open [r (java.io.StringReader. "test")]
                        :result)]
           (is (= :result result))))

       (testing "with StringReader"
         (let [content (with-open [r (java.io.StringReader. "hello")]
                         (slurp r))]
           (is (= "hello" content))))

       (testing "resource available in body"
         (with-open [r (java.io.StringReader. "test")]
           (is (some? r)))))

     :cljs
     (deftest test-with-open
       (testing "with-open not in ClojureScript"
         (is true)))))
