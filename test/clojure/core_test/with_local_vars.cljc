(ns clojure.core-test.with-local-vars
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-local-vars
  (deftest test-with-local-vars
    (testing "create and use local var"
      (is (= 42 (with-local-vars [x 42] @x))))

    (testing "multiple local vars"
      (is (= 3 (with-local-vars [x 1 y 2] (+ @x @y)))))

    (testing "modify with var-set"
      (is (= 10 (with-local-vars [x 5] (var-set x 10) @x))))

    (testing "factorial example"
      (let [factorial (fn [n]
                        (with-local-vars [acc 1 cnt n]
                          (while (> @cnt 0)
                            (var-set acc (* @acc @cnt))
                            (var-set cnt (dec @cnt)))
                          @acc))]
        (is (= 120 (factorial 5)))))

    (testing "accumulator"
      (is (= 15 (with-local-vars [sum 0]
                  (doseq [i (range 1 6)]
                    (var-set sum (+ @sum i)))
                  @sum))))

    (testing "shadows outer binding"
      (let [x 100]
        (is (= 42 (with-local-vars [x 42] @x)))
        (is (= 100 x))))

    (testing "with nil value"
      (is (nil? (with-local-vars [x nil] @x))))

    (testing "counter example"
      (is (= 10 (with-local-vars [counter 0]
                  (dotimes [_ 10]
                    (var-set counter (inc @counter)))
                  @counter))))

    (testing "returns last expression"
      (is (= :result (with-local-vars [x 1] :result))))))
