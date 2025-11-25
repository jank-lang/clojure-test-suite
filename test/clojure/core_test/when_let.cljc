(ns clojure.core-test.when-let
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-let
                 (deftest test-when-let
                   (testing "basic single-binding tests using vectors or nil"
                     (is (= [0 1 2 3 4] (when-let [x [0 1 2 3 4] ] x)))
                     (is (not (nil? (when-let [x [nil]] x))))
                     (is (= [] (when-let [x []] x)))
                     (is (nil? (when-let [x nil] x))))
                   (testing "basic single-binding tests using seqs"
                     (is (= '(0 1 2 3 4) (when-let [x (range 5)] x))))
                   (testing "seq is only called once"
                     (let [calls (atom 0)
                           seq-fn (fn s [] (lazy-seq
                                             (swap! calls inc)
                                             (cons 1 (s))))
                           s (take 5 (seq-fn))]
                       (is (= '(1 1 1 1 1) (when-let [x s] x)))
                       (is (= @calls 5))))
                   (testing "without a body, truth doesn't matter"
                     (is (nil? (when-let [x nil])))
                     (is (nil? (when-let [x [false]])))
                     (is (nil? (when-let [x [true]]))))
                   (testing "when has an implicit `do`"
                     (let [counter (atom 0)]
                       (is (= :bar (when-let [x (range 5)]
                                     (swap! counter inc)
                                     (swap! counter inc)
                                     (swap! counter inc)
                                     :bar)))
                       (is (= 3 @counter))))
                   #?(:cljs nil ; Skipped due to ClojureScript's atypical macro expansion.
                      :bb nil ; Skipped because of Babashka issue https://github.com/babashka/babashka/issues/1894
                      :default (testing "when-let accepts exactly two"
                                 (is (thrown? Exception
                                              (macroexpand
                                                '(when-let [x (range 5) y (range 5)]))))))))
