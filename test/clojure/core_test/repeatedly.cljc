(ns clojure.core-test.repeatedly
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists repeatedly
  (deftest test-repeatedly
    (testing "Side effecting"
      (let [state (atom 0)]
        (is (= '(1 2 3 4 5) (repeatedly 5 #(swap! state inc))))
        (is (= 5 @state)))
      (testing "handles mid failures gracefully"
        (let [state (atom 0)
              fails-second-run (fn [n] (if (> @n 0)
                                        (throw (ex-info "expected" {}))
                                        (swap! n inc)))]
          (is (p/thrown? (last (repeatedly 2 #(fails-second-run state)))))
          (is (= #?(;; phel doesn't seem to handle mid failures gracefully
                    :phel    0
                    :default 1)
                 @state))))))

    (testing "Single argument"
      (is (= 0 (first (repeatedly +))))
      #?(:phel nil ;; phel's ConstantFolder evaluates (repeatedly identity) at compile time, calling identity with 0 args
         :default
         (testing "is lazy"
           (let [call (repeatedly identity)]
             (is (not (realized? call)))
             (is (p/lazy-seq? call))))))

    (testing "Two arguments"
      (testing "is lazy"
        (let [call (repeatedly 1000 identity)]
          (is (not (realized? call)))
          (is (p/lazy-seq? call))))
      (testing "zero returns empty list"
        (is (= '() (repeatedly 0 +))))
      (testing "natural numbers"
        (is (= '(0) (repeatedly 1 +)))
        (is (= '(0 0 0) (repeatedly 3 +))))
      (testing "non-integer numbers"
        (is (= '(0 0) (repeatedly 1.5 +)))
        (is #?(:cljs true ;; cljs doesn't implement ratios
               :phel (p/thrown? (repeatedly 1/2 +))
               :default (= '(0) (repeatedly 1/2 +))))
        (is (= '() (repeatedly -1 +)))))

    (testing "Exception cases"
      (testing "non-functions throw"
        (are [x]
            (p/thrown? (first (repeatedly x)))
          \a
          ""
          #""
          (atom 0)
          '()))
      (testing "non-numeric first arguments throw"
        (is #?(:cljr    (= 0 (first (repeatedly \a +)))
               :default (p/thrown? (first (repeatedly \a +)))))
        (are [x] 
            (p/thrown? (first (repeatedly x +)))
          ""
          #""
          (fn [])
          (atom 0)
          {}
          #{}
          '()
          []))))
