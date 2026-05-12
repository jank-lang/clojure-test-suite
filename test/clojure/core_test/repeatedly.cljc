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
          (try
            (last (repeatedly 2 #(fails-second-run state)))
            (catch #?(:cljs    :default
                      :default Exception) _ nil))
          (is (= @state 1))))
      (testing "is lazy"
        (let [state (atom 0)]
          #_{:clj-kondo/ignore [:redundant-do]}
          (do (repeatedly #(swap! state inc))
              (is (= 0 @state))))))

    (testing "Single argument"
      (is (= 0 (first (repeatedly +)))))

    (testing "Two arguments"
      (testing "zero returns empty list"
        (is (= '() (repeatedly 0 +))))
      (testing "natural numbers"
        (is (= '(0) (repeatedly 1 +)))
        (is (= '(0 0 0) (repeatedly 3 +))))
      (testing "non-integer numbers"
        (is (= '(0 0) (repeatedly 1.5 +)))
        (is (= '(0)
               #?(:cljs '(0) ;; cljs doesn't implement ratios
                  :default (repeatedly 1/2 +))))
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
          [])))))
