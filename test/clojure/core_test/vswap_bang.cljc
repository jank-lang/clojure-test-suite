(ns clojure.core-test.vswap-bang
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vswap!
  (deftest test-vswap-bang
    (testing "swap with inc"
      (let [v (volatile! 10)]
        (vswap! v inc)
        (is (= 11 @v))))

    (testing "returns new value"
      (let [v (volatile! 10)]
        (is (= 11 (vswap! v inc)))))

    (testing "swap with dec"
      (let [v (volatile! 10)]
        (vswap! v dec)
        (is (= 9 @v))))

    (testing "swap with + and one arg"
      (let [v (volatile! 10)]
        (vswap! v + 5)
        (is (= 15 @v))))

    (testing "swap with + and multiple args"
      (let [v (volatile! 10)]
        (vswap! v + 5 3 2)
        (is (= 20 @v))))

    (testing "swap with * and one arg"
      (let [v (volatile! 10)]
        (vswap! v * 2)
        (is (= 20 @v))))

    (testing "swap with * and multiple args"
      (let [v (volatile! 2)]
        (vswap! v * 3 4)
        (is (= 24 @v))))

    (testing "swap with - and one arg"
      (let [v (volatile! 100)]
        (vswap! v - 30)
        (is (= 70 @v))))

    (testing "swap with - and multiple args"
      (let [v (volatile! 100)]
        (vswap! v - 30 10 5)
        (is (= 55 @v))))

    (testing "swap with conj"
      (let [v (volatile! [])]
        (vswap! v conj 1)
        (is (= [1] @v))
        (vswap! v conj 2)
        (is (= [1 2] @v))))

    (testing "swap with assoc"
      (let [v (volatile! {})]
        (vswap! v assoc :a 1)
        (is (= {:a 1} @v))
        (vswap! v assoc :b 2)
        (is (= {:a 1 :b 2} @v))))

    (testing "swap with dissoc"
      (let [v (volatile! {:a 1 :b 2 :c 3})]
        (vswap! v dissoc :b)
        (is (= {:a 1 :c 3} @v))))

    (testing "swap with update"
      (let [v (volatile! {:count 0})]
        (vswap! v update :count inc)
        (is (= {:count 1} @v))
        (vswap! v update :count + 5)
        (is (= {:count 6} @v))))

    (testing "swap with not"
      (let [v (volatile! true)]
        (vswap! v not)
        (is (false? @v))
        (vswap! v not)
        (is (true? @v))))

    (testing "swap with str"
      (let [v (volatile! "hello")]
        (vswap! v str " world")
        (is (= "hello world" @v))))

    (testing "swap with custom function"
      (let [v (volatile! 10)
            double (fn [x] (* x 2))]
        (vswap! v double)
        (is (= 20 @v))))

    (testing "swap with anonymous function"
      (let [v (volatile! 5)]
        (vswap! v #(* % %))
        (is (= 25 @v))))

    (testing "multiple swaps"
      (let [v (volatile! 0)]
        (vswap! v inc)
        (vswap! v inc)
        (vswap! v inc)
        (is (= 3 @v))))

    (testing "swap in loop"
      (let [v (volatile! 0)]
        (dotimes [_ 10]
          (vswap! v inc))
        (is (= 10 @v))))

    (testing "swap with identity"
      (let [v (volatile! 42)]
        (vswap! v identity)
        (is (= 42 @v))))

    (testing "swap from nil"
      (let [v (volatile! nil)]
        (vswap! v (constantly 42))
        (is (= 42 @v))))

    (testing "swap with constantly"
      (let [v (volatile! 1)]
        (vswap! v (constantly 99))
        (is (= 99 @v))))

    (when-var-exists volatile?
      (testing "swap preserves volatile identity"
        (let [v (volatile! 1)]
          (vswap! v inc)
          (is (volatile? v)))))

    (testing "swap with into"
      (let [v (volatile! [])]
        (vswap! v into [1 2 3])
        (is (= [1 2 3] @v))))

    (testing "swap with merge"
      (let [v (volatile! {:a 1})]
        (vswap! v merge {:b 2})
        (is (= {:a 1 :b 2} @v))))

    (testing "swap with comp"
      (let [v (volatile! 5)]
        (vswap! v (comp inc inc inc))
        (is (= 8 @v))))

    (testing "swap accumulator pattern"
      (let [v (volatile! 0)]
        (doseq [i (range 1 11)]
          (vswap! v + i))
        (is (= 55 @v))))

    (testing "multiple volatiles swap independently"
      (let [v1 (volatile! 0)
            v2 (volatile! 10)]
        (vswap! v1 inc)
        (vswap! v2 dec)
        (is (= 1 @v1))
        (is (= 9 @v2))))

    (testing "swap with max"
      (let [v (volatile! 5)]
        (vswap! v max 10)
        (is (= 10 @v))
        (vswap! v max 3)
        (is (= 10 @v))))

    (testing "swap with min"
      (let [v (volatile! 5)]
        (vswap! v min 3)
        (is (= 3 @v))
        (vswap! v min 10)
        (is (= 3 @v))))

    (testing "swap with reverse"
      (let [v (volatile! [1 2 3])]
        (vswap! v reverse)
        (is (= '(3 2 1) @v))))

    (testing "swap with sort"
      (let [v (volatile! [3 1 2])]
        (vswap! v sort)
        (is (= '(1 2 3) @v))))))
