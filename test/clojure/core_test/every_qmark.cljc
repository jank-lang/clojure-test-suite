(ns clojure.core-test.every-qmark
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.test :refer [are deftest is testing]]))

(defn boom! [x] (throw (ex-info "Boom!" {:x x})))

(when-var-exists every?
  (deftest test-every?
    (testing "Basic Predicates and Collections"
      (is (false? (every? odd? [1 2 3])))
      (is (true? (every? odd? [1 3 5])))
      (is (false? (every? even? #{1 2 3})))
      (is (true? (every? even? #{2 4 6}))))

    (testing "Empty Collections are Always True"
      (are [coll] (true? (every? boom! coll))
        nil
        []
        '()))

    (testing "Maps and Sets as Predicates"
      (are [expected pred] (= expected (every? pred [:a :b :c]))
        true #{:a :b :c}
        false #{:a :c}
        true {:a "a" :b "b" :c "c"}
        false {:a "a" :b nil :c "c"}
        false {:a "a" :b false :c "c"}))

    (testing "Nil and False"
      (is (false? (every? identity [nil])))
      (is (false? (every? identity [false])))
      (is (false? (every? #{nil} [nil])))
      (is (false? (every? #{false} [false])))
      (is (true? (every? {false :false nil :nil} [false nil]))))

    (testing "Truthy Results in True"
      (are [x] (true? (every? identity [x]))
        true
        :foo
        'foo
        ""
        0
        []
        '()
        ##NaN))

    (testing "Terminates on First Falsey"
      (letfn [(maybe-boom [x]
                (case x
                  1 true
                  2 false
                  (boom! x)))]
        (is (false? (every? maybe-boom [1 2 3])))
        (is (p/thrown? (every? maybe-boom [1 3])))))

    (testing "Infinite Seq"
      (is (false? (every? even? (range)))))

    (testing "Bad Shape"
      (is (p/thrown? (every? (fn [_]) :not-a-seq)))
      (is (p/thrown? (every? "not-a-fn" [1])))
      (is (true? (every? "not-a-fn" []))))))
