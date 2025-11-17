(ns clojure.core-test.doseq
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))


(when-var-exists doseq
  (deftest test-doseq
    (testing "returns nil"
      (is (nil? (doseq [x [1 2 3]] x))))
    (testing "side effects happen in order for a single binding"
      (let [acc (atom [])]
        (doseq [x [1 2 3]] (swap! acc conj x))
        (is (= [1 2 3] @acc))))
    (testing "nil input: body not executed; result nil"
      (let [acc (atom :unchanged)]
        (is (nil? (doseq [_ nil] (reset! acc :changed))))
        (is (= :unchanged @acc))))
    (testing "empty input: body not executed; result nil"
     (let [acc (atom 0)]
       (is (nil? (doseq [_ [] y [1 2 3]] (swap! acc + y))))
       (is (= 0 @acc)))
     (let [acc (atom 0)]
       (is (nil? (doseq [_ []] (swap! acc inc))))
       (is (= 0 @acc))))
    (testing "nested bindings iterate like a nested loop (leftmost outermost)"
      (let [acc (atom [])]
        (doseq [x [1 2]
                y [10 20]]
          (swap! acc conj [x y]))
        (is (= [[1 10] [1 20] [2 10] [2 20]] @acc))))
    (testing "vector destructuring in bindings"
      (let [acc (atom [])]
        (doseq [[k v] [[:a 1] [:b 2]]]
          (swap! acc conj [k v]))
        (is (= [[:a 1] [:b 2]] @acc))))
    (testing "map destructuring in bindings"
     (let [acc (atom {})]
        (doseq [[k v] {:a 0 :b 1}]
          (swap! acc assoc k v))
        (is (= {:a 0 :b 1} @acc))))
    (testing ":when filters values"
      (let [acc (atom [])]
        (doseq [x (range 6)
                :when (odd? x)]
          (swap! acc conj x))
        (is (= [1 3 5] @acc))))
    (testing ":let introduces locals"
      (let [acc (atom [])]
        (doseq [x [1 2 3]
                :let [y (* 2 x)]]
          (swap! acc conj y))
        (is (= [2 4 6] @acc))))
    (testing ":while stops early"
      (let [acc (atom [])]
        (doseq [x (range) :while (< x 3)]
          (swap! acc conj x))
        (is (= [0 1 2] @acc))))
    (testing "mixing :let and :when across nested bindings"
      (let [acc (atom [])]
        (doseq [x (range 4)
                :let [y (* x x)]
                y [y]     
                :when (pos? y)]
          (swap! acc conj y))
        (is (= [1 4 9] @acc))))))

