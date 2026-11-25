(ns clojure.core-test.vreset-bang
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/vreset!

  ;; DOCSTRING:
  ;; clojure.core/vreset!
  ;; [vol newval]
  ;; Sets the value of volatile to newval without regard for the
  ;; current value. Returns newval.

  (deftest test-vreset!
    (testing "returns newval"
      (are [v] (= v (vreset! (volatile! :initial) v))
        nil
        false
        0
        1.5
        \c
        ""
        :kw
        'sym
        '(one two three)
        []
        {}
        #{1 2 3}))

    (testing "sets the value of volatile to newval"
      (let [v (volatile! :initial)]
        (is (= :next (vreset! v :next)))
        (is (= :next @v))))

    (testing "without regard for the current value"
      (let [v (volatile! :initial)]
        (is (= :first (vreset! v :first)))
        (is (= :first @v))
        (is (= :second (vreset! v :second)) "the current value plays no part")
        (is (= :second @v))
        (is (= :second (vreset! v :second)) "resetting to the current value is fine")
        (is (= :second @v))
        (is (nil? (vreset! v nil)))
        (is (nil? @v) "nil is a value, not an absence of one")
        (is (= :after-nil (vreset! v :after-nil)))
        (is (= :after-nil @v))))

    (testing "the value itself is stored, not a copy of it"
      (let [inner (volatile! :inner)
            outer (volatile! :outer)]
        (is (identical? inner (vreset! outer inner)))
        (is (identical? inner @outer)))
      (let [v (volatile! :old)
            m (with-meta [1 2 3] {:m true})]
        (is (identical? m (vreset! v m)))
        (is (= {:m true} (meta @v)))))

    (testing "an infinite lazy sequence is stored without being realized"
      (let [v (volatile! :old)
            r (range)]
        (is (identical? r (vreset! v r)))
        (is (identical? r @v))))

    (testing "the returned newval can be fed to another vreset!"
      (let [v1 (volatile! :old)
            v2 (volatile! :old)]
        (is (= :nested (vreset! v2 (vreset! v1 :nested))))
        (is (= :nested @v1))
        (is (= :nested @v2))))))
