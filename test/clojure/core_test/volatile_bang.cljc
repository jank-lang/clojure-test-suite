(ns clojure.core-test.volatile-bang
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/volatile!

  ;; DOCSTRING:
  ;; clojure.core/volatile!
  ;; [val]
  ;; Creates and returns a Volatile with an initial value of val.

  (deftest test-volatile!
    (testing "returns a Volatile with an initial value of val"
      (are [v] (= v @(volatile! v))
        nil
        false
        0
        1.5
        ""
        "string"
        :kw
        'sym
        '()
        '(one two three)
        []
        [1 2 3]
        {}
        {:k "value"}
        #{1 2 3}
        #?(:cljs "cljs is the only (?) dialect without ratios"
           :default 111/7))

      (testing "each call creates its own Volatile, not a shared instance"
        (is (not (identical? (volatile! :val) (volatile! :val))))))

    (testing "the result is a Volatile"
      (is (volatile? (volatile! :initial))))

    (testing "the value itself is stored, not a copy of it"
      (let [inner (volatile! :inner)
            outer (volatile! inner)]
        (is (identical? inner @outer))
        (is (= :inner @@outer))))

    (testing "the metadata of val survives the round trip"
      (let [v (with-meta [1 2 3] {:m true})]
        (is (= {:m true} (meta @(volatile! v))))))

    (testing "a lazy sequence is stored without being realized"
      (let [r (range)]
        (is (identical? r @(volatile! r)))))))
