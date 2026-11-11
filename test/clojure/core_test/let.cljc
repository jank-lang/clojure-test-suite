(ns clojure.core-test.let
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists let
  (def let-global :global-value)

  (deftest test-let
    (testing "bindings are local and the body's value is returned"
      (is (= 1 (let [x 1] x)))
      (is (= :a (let [x :a] x)))
      (is (= "str" (let [x "str"] x)))
      (is (= nil (let [x nil] x)))
      (is (= false (let [x false] x)))
      (is (= [1 2] (let [x 1 y 2] [x y]))))

    (testing "let has an implicit `do`"
      (is (= 3 (let [x 1] x 2 3)))
      (let [order (volatile! [])]
        (is (= :last (let [x (vswap! order conj :x)]
                       (vswap! order conj :first-body)
                       (vswap! order conj :second-body)
                       :last)))
        (is (= [:x :first-body :second-body] @order))))

    (testing "when no body forms, return nil, but the bindings are still evaluated"
      (is (nil? (let [x 1])))
      (is (nil? (let [x 1 y 2])))
      (let [counter (volatile! 0)]
        (is (nil? (let [x (vswap! counter inc)])))
        (is (= 1 @counter))))

    (testing "an empty binding vector is legal"
      (is (= 1 (let [] 1)))
      (is (nil? (let [])))
      (is (= 2 (let [] 1 2))))

    (testing "bindings are evaluated in order and can refer to earlier bindings"
      (is (= [1 1] (let [x 1 y x] [x y])))
      (is (= [1 2] (let [x 1 y (inc x)] [x y])))
      (is (= [[1 2] 2] (let [x 1 y [x (inc x)] z (count y)] [y z])))
      (is (= 6 (let [a 1 b (+ a 1) c (+ b 2) d (+ c 2)] d)))
      (let [order (volatile! [])]
        (let [_a (vswap! order conj :a)
              _b (vswap! order conj :b)]
          (vswap! order conj :body))
        (is (= [:a :b :body] @order))))

    (testing "a binding may shadow an earlier binding of the same name"
      (is (= 2 (let [x 1 x 2] x)))
      (is (= 2 (let [x 1 x (inc x)] x)))
      (is (= [:inner :inner] (let [x :outer x :inner] [x x]))))

    (testing "each binding is captured by value"
      (is (= 1 (let [x 1 f (fn [] x) x 2] (f))))
      (is (= 1 (let [x 1 f (fn [] x) x (inc x)] (f))))
      (is (= [1 1] (let [x 1 f (fn [] x) y x x 2] [(f) y])))
      (is (= [1 3] (let [x 1 f (fn [y] (+ x y))] [x (f 2)]))))

    (testing "let introduces a new scope"
      (is (= [:local-value :global-value]
             [(let [let-global :local-value] let-global) let-global]))
      (is (= [2 1] (let [x 1] [(let [x 2] x) x])))
      (is (= [3 1] (let [x 1] [(let [y (+ x 2)] y) x])))
      (is (= 2 ((fn [x] (let [x (inc x)] x)) 1)))
      (is (= 1 ((fn [x] (let [y (inc x)] x)) 1))))

    (testing "nested lets"
      (is (= 3 (let [x 1] (let [y 2] (+ x y)))))
      (is (= [1 2 3] (let [x 1] (let [y 2 z 3] [x y z]))))
      (is (= [1 3 2] (let [x 1 y 2] (let [y (+ x y)] [x y (let [y (* 2 x)] y)])))))

    (testing "bindings do not force lazy sequences"
      (is (= '(0 1 2) (let [s (range)] (take 3 s))))
      (is (= 1 (let [s (map inc (range))] (first s))))
      (is (= '(2 3 4) (let [s (map inc (range 1 4))] s))))

    (testing "invalid binding forms are rejected"
      (are [form] (p/thrown? (eval form))
        '(let [x] x)        ; odd number of binding forms
        '(let [x 1 y] x)    ; odd number of binding forms
        '(let (x 1) x)      ; binding forms must be in a vector
        '(let [1 2] 3)      ; binding target must be a symbol
        '(let [:kw 1] :kw)  ; binding target must be a symbol
        '(let)))))
