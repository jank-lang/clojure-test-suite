(ns clojure.core-test.every-qmark
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.test :refer [are deftest is testing]]))

(defn boom! [x] (throw (ex-info "Boom!" {:x x})))

(defn tests [every-fn]
  (testing "Basic Predicates and Collections"
    (is (false? (every-fn odd? [1 2 3])))
    (is (true? (every-fn odd? [1 3 5])))
    (is (false? (every-fn even? #{1 2 3})))
    (is (true? (every-fn even? #{2 4 6}))))

  (testing "Empty Collections"
    (are [coll] (true? (every-fn boom! coll))
      nil
      []
      '()))

  (testing "Maps and Sets as Predicates"
    (are [expected pred] (= expected (every-fn pred [:a :b :c]))
      true #{:a :b :c}
      false #{:a :c}
      true {:a "a" :b "b" :c "c"}
      false {:a "a" :b nil :c "c"}
      false {:a "a" :b false :c "c"}))

  (testing "Nil and False"
    (is (false? (every-fn identity [nil])))
    (is (false? (every-fn identity [false])))
    (is (false? (every-fn #{nil} [nil])))
    (is (false? (every-fn #{false} [false])))
    (is (true? (every-fn {false :false nil :nil} [false nil]))))

  (testing "Truthy Values"
    (are [x] (true? (every-fn identity [x]))
      true
      :foo
      'foo
      ""
      0
      []
      '()
      ##NaN))

  (testing "Early Termination"
    (letfn [(maybe-boom [x]
              (case x
                1 true
                2 false
                (boom! x)))]
      (is (false? (every-fn maybe-boom [1 2 3])))
      (is (p/thrown? (every-fn maybe-boom [1 3])))))

  (testing "Infinite Seq"
    (is (false? (every-fn even? (range)))))

  (testing "Bad Shape"
    (is (p/thrown? (every-fn (fn [_]) :not-a-seq)))
    (is (p/thrown? (every-fn "not-a-fn" [1])))
    (is (true? (every-fn "not-a-fn" [])))))

(when-var-exists every?
  (deftest test-every?
    (tests every?)))
