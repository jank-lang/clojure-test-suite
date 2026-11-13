(ns clojure.core-test.reset-bang
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/reset!

  ;; DOCSTRING:
  ;; clojure.core/reset!
  ;; [atom newval]
  ;; Sets the value of atom to newval without regard for the
  ;; current value. Returns newval.

  (deftest test-reset!
    (testing "returns newval"
      (let [atm (atom 0)]
        (is (= 1 (reset! atm 1)))
        (is (= 1 @atm))
        ;; resetting to the value already in the atom still returns it
        (is (= 1 (reset! atm 1)))
        (is (= 1 @atm))))

    (testing "sets the value without regard for the current value"
      (let [atm (atom 100)]
        (is (= 101 (reset! atm 101)))
        (is (= 101 @atm))
        ;; the new value may be smaller than the old one
        (is (= 1 (reset! atm 1)))
        (is (= 1 @atm)))
      ;; the new value replaces the old one outright, with no merging
      (let [atm (atom {:a 1 :b 2})]
        (is (= {:c 3} (reset! atm {:c 3})))
        (is (= {:c 3} @atm))
        (is (= {} (reset! atm {})))
        (is (= {} @atm))))

    (testing "any value can be stored, including nil and false"
      (are [v] (let [atm (atom ::untouched)]
                 (and (= v (reset! atm v))
                      (= v @atm)))
        nil
        false
        true
        0
        ""
        'sym
        :kw
        '()
        []
        {}
        #{}))

    (testing "the value passed in is stored and returned as is"
      (let [atm (atom nil)
            v (with-meta [1 2 3] {:m true})]
        (is (identical? v (reset! atm v)))
        (is (identical? v @atm))))

    (testing "an infinite lazy sequence is stored without being realized"
      (let [atm (atom nil)
            r (range)]
        (is (identical? r (reset! atm r)))
        (is (identical? r @atm))
        (is (= 0 (first @atm)))))

    (testing "metadata of the atom itself is untouched"
      (let [atm (atom 1 :meta {:a 1})]
        (is (= 2 (reset! atm 2)))
        (is (= 2 @atm))
        (is (= {:a 1} (meta atm)))))

    (testing "an accepted value passes the atom's validator"
      (let [atm (atom 0 :validator even?)]
        (is (= 2 (reset! atm 2)))
        (is (= 2 @atm))
        (is (= 46 (reset! atm 46)))
        (is (= 46 @atm))))

    (testing "a rejected value throws and leaves the atom unchanged"
      (let [atm (atom 0 :validator even?)]
        (is (p/thrown? (reset! atm 1)))
        (is (= 0 @atm))
        (is (= 2 (reset! atm 2)))
        (is (p/thrown? (reset! atm 3)))
        (is (= 2 @atm))))

    (testing "an exception thrown by the validator propagates"
      (let [atm (atom 0 :validator (fn [v] (or (zero? v) (throw (ex-info "boom" {})))))]
        (is (p/thrown? (reset! atm 1)))
        (is (= 0 @atm))))

    (testing "watches see the old and the new value"
      (let [atm (atom 0)
            seen (atom [])
            k ::watch]
        (add-watch atm k (fn [key _ref old new] (swap! seen conj [key old new])))
        (is (= 1 (reset! atm 1)))
        (is (= :two (reset! atm :two)))
        (is (= [[k 0 1] [k 1 :two]] @seen))))

    (testing "an argument that is not an atom throws"
      (is (p/thrown? (reset! 1 2)))
      (is (p/thrown? (reset! nil 2)))
      (is (p/thrown? (reset! "not an atom" 2))))))
