(ns clojure.core-test.volatile-bang
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists volatile!
  (deftest test-volatile-bang
    (testing "create with initial value"
      (let [v (volatile! 42)]
        (is (= 42 @v))))

    (testing "create with nil"
      (let [v (volatile! nil)]
        (is (nil? @v))))

    (testing "create with string"
      (let [v (volatile! "hello")]
        (is (= "hello" @v))))

    (testing "create with collection"
      (let [v (volatile! [1 2 3])]
        (is (= [1 2 3] @v)))
      (let [v (volatile! {:a 1})]
        (is (= {:a 1} @v)))
      (let [v (volatile! #{1 2})]
        (is (= #{1 2} @v))))

    (testing "create with function"
      (let [v (volatile! +)]
        (is (fn? @v))
        (is (= 3 (@v 1 2)))))

    (testing "deref returns current value"
      (let [v (volatile! 100)]
        (is (= 100 @v))
        (is (= 100 (deref v)))))

    (testing "multiple volatiles are independent"
      (let [v1 (volatile! 1)
            v2 (volatile! 2)]
        (is (= 1 @v1))
        (is (= 2 @v2))))

    (testing "create with keyword"
      (let [v (volatile! :test)]
        (is (= :test @v))))

    (testing "create with symbol"
      (let [v (volatile! 'foo)]
        (is (= 'foo @v))))

    (testing "create with boolean"
      (let [v-true (volatile! true)
            v-false (volatile! false)]
        (is (true? @v-true))
        (is (false? @v-false))))

    (testing "create with number types"
      (let [v-int (volatile! 42)
            v-long (volatile! 42N)
            v-float (volatile! 3.14)]
        (is (= 42 @v-int))
        (is (= 42N @v-long))
        (is (= 3.14 @v-float)))
      #?(:clj
         (let [v-ratio (volatile! 1/2)]
           (is (= 1/2 @v-ratio)))))

    (testing "create with zero"
      (let [v (volatile! 0)]
        (is (= 0 @v))))

    (testing "create with negative numbers"
      (let [v (volatile! -42)]
        (is (= -42 @v))))

    (testing "create with empty collection"
      (let [v-vec (volatile! [])
            v-map (volatile! {})
            v-set (volatile! #{})]
        (is (= [] @v-vec))
        (is (= {} @v-map))
        (is (= #{} @v-set))))

    (testing "create with nested structure"
      (let [v (volatile! {:a [1 2 {:b 3}]})]
        (is (= {:a [1 2 {:b 3}]} @v))))

    (testing "volatile creation in let binding"
      (let [x 10
            v (volatile! x)]
        (is (= 10 @v))))

    (testing "volatile with lazy sequence"
      (let [v (volatile! (map inc [1 2 3]))]
        (is (= '(2 3 4) @v))))

    (testing "multiple derefs return same value"
      (let [v (volatile! 42)]
        (is (= @v @v @v 42))))

    (testing "create in function"
      (defn make-volatile [x]
        (volatile! x))
      (let [v (make-volatile 99)]
        (is (= 99 @v))))

    (testing "volatile with computed value"
      (let [v (volatile! (+ 1 2 3))]
        (is (= 6 @v))))

    (testing "create with character"
      #?(:clj
         (let [v (volatile! \a)]
           (is (= \a @v)))))

    (testing "create with large number"
      (let [v (volatile! 1000000000000)]
        (is (= 1000000000000 @v))))))
