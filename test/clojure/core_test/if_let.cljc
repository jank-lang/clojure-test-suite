(ns clojure.core-test.if-let
(:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists if-let
                 (deftest test-if-let
                   (testing "basic single-binding tests using vectors or nil"
                     (is (= [0 1 2 3 4] (if-let [x [0 1 2 3 4] ] x :else)))
                     (is (not (nil? (if-let [x [nil]] x :else))))
                     (is (= [] (if-let [x []] x :else)))
                     (is (= :else (if-let [x nil] x :else))))
                   (testing "basic single-binding tests using seqs"
                     (is (= '(0 1 2 3 4) (if-let [x (range 5)] x :else))))
                   (testing "unlike, when-some, we're looking for not-nil specifically, so false evaluates"
                     (is (= :else (if-let [x false] x :else))))
                   (testing "seq is only called once"
                     (let [calls (atom 0)
                           seq-fn (fn s [] (lazy-seq
                                             (swap! calls inc)
                                             (cons 1 (s))))
                           s (take 5 (seq-fn))]
                       (is (= '(1 1 1 1 1) (if-let [x s] x :else)))
                       (is (= @calls 5))))
                   (comment (testing "without a body, truth doesn't matter"
                              (is (nil? (if-let [x nil])))
                              (is (nil? (if-let [x [false]])))
                              (is (nil? (if-let [x [true]])))))
                   #?(:cljs nil ; Skipped due to ClojureScript's atypical macro expansion.
                      ;; :bb nil ; Skipped because of Babashka issue https://github.com/babashka/babashka/issues/1894
                      :default (testing "if-let accepts exactly two bindings"
                                 (is (thrown? Exception
                                              (macroexpand
                                                '(if-let [x (range 5) y (range 5)]))))))
                   #?(:cljs nil ; Skipped due to ClojureScript's atypical macro expansion.
                      ;; :bb nil ; Skipped because of Babashka issue https://github.com/babashka/babashka/issues/1894
                      :default (testing "if-let accepts exactly two bindings"
                                 (is (thrown? Exception
                                              (macroexpand
                                                '(if-let [x (range 5) y (range 5)]))))))
                   ))
