(ns clojure.core-test.when-first
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))


(when-var-exists when-first
  (deftest test-when-first
    (testing "Basic single-binding tests using vectors or nil"
      (is (= 0 (when-first [x [0 1 2 3 4] ] x)))
      (is (nil? (when-first [x [nil]] x)))
      (is (nil? (when-first [x []] x)))
      (is (nil? (when-first [x nil] x))))
    (testing "Basic single-binding tests using seqs"
      (is (= 0 (when-first [x (range 5) ] x)))
      (is (nil? (when-first [x (repeat nil)] x))))
    (testing "Seq only called once"
      (let [calls (atom 0)
            seq-fn (fn s [] (lazy-seq
                              (swap! calls inc)
                              (cons 1 (s))))
            s (seq-fn)]
        (is (= 1 (when-first [x s] x)))
        (is (= @calls 1))))
    (testing "without a body, truth doesn't matter"
      (is (nil? (when-first [x nil])))
      (is (nil? (when-first [x [false]])))
      (is (nil? (when-first [x [true]]))))
    (testing "when has an implicit `do`"
      (let [counter (atom 0)]
        (is (= :bar (when-first [x (range 5)] 
                   (swap! counter inc) 
                   (swap! counter inc)
                   (swap! counter inc)
                   :bar)))
        (is (= 3 @counter))))))
