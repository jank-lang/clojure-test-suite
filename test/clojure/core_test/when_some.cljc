
(ns clojure.core-test.when-some
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-some
  (deftest test-when-some
    (testing "basic single-binding tests using vectors or nil"
      (is (= [0 1 2 3 4] (when-some [x [0 1 2 3 4] ] x)))
      (is (not (nil? (when-some [x [nil]] x))))
      (is (= [] (when-some [x []] x)))
      (is (nil? (when-some [x nil] x))))
    (testing "basic single-binding tests using seqs"
      (is (= '(0 1 2 3 4) (when-some [x (range 5)] x))))
    (testing "unlike when-let, we're looking for not-nil specifically, so false evaluates"
      (is (= false (when-some [x false] x))))
    (testing "seq is only called once"
      (let [calls (atom 0)
            seq-fn (fn s [] (lazy-seq
                              (swap! calls inc)
                              (cons 1 (s))))
            s (take 5 (seq-fn))]
        (is (= '(1 1 1 1 1) (when-some [x s] x)))
        (is (= @calls 5))))
    (testing "without a body, truth doesn't matter"
      (is (nil? (when-some [x nil])))
      (is (nil? (when-some [x [false]])))
      (is (nil? (when-some [x [true]]))))))
