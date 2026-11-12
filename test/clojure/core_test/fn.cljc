(ns clojure.core-test.fn
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/fn

  ;; DOCSTRING:
  ;; clojure.core/fn
  ;; [& sigs]
  ;; params => positional-params*, or positional-params* & rest-param
  ;; positional-param => binding-form
  ;; rest-param => binding-form
  ;; binding-form => name, or destructuring-form
  ;; 
  ;; Defines a function.
  ;; 
  ;; See https://clojure.org/reference/special_forms#fn for more information

  (def fn-test-shadowed :global)

  (deftest test-fn
    (testing "basic forms with a single parameter list"
      (is (= 42 ((fn [] 42))))
      (is (= :param ((fn [x] x) :param)))
      (is (= [1 2] ((fn [x y] [x y]) 1 2)))
      (is (= 3 ((fn [a b c] (+ a b c)) 1 1 1))))

    (testing "the body is an implicit do"
      (is (= :last ((fn [] :first :last))))
      (is (= 2 ((fn [x] x 1 2) 0)))
      (is (nil? ((fn []))))
      (is (nil? ((fn [_x]) 1))))

    (testing "arguments are locals that shadow outer bindings"
      (is (= :global fn-test-shadowed))
      (is (= :param ((fn [fn-test-shadowed] fn-test-shadowed) :param)))
      (is (= :global fn-test-shadowed))
      (is (= [:param :global] ((fn [x] [x fn-test-shadowed]) :param)))
      (is (= 2 ((fn [x] (let [x (inc x)] x)) 1)))
      (is (= [1 2] ((fn [x] (let [y (inc x)] [x y])) 1))))

    (testing "closures capture the surrounding lexical environment"
      (let [x 1
            f (fn [] x)
            x 2]
        (is (= [1 2] [(f) x])))
      (is (= 3 (((fn [x] (fn [y] (+ x y))) 1) 2)))
      (let [counter (volatile! 0)
            inc! (fn [] (vswap! counter inc))]
        (inc!)
        (inc!)
        (is (= 2 @counter))))

    (testing "functions are values"
      (is (fn? (fn [])))
      (is (ifn? (fn [])))
      (is (= '(1 4 9) (map (fn [x] (* x x)) [1 2 3])))
      (is (= 6 (reduce (fn [acc x] (+ acc x)) 0 [1 2 3])))
      (is (= 3 ((fn [f x] (f x)) inc 2)))
      (is (= 0 (apply (fn [] 0) nil))))

    (testing "multiple arities are dispatched by argument count"
      (let [f (fn ([] :zero) ([_x] :one) ([_x _y] :two))]
        (is (= :zero (f)))
        (is (= :one (f 1)))
        (is (= :two (f 1 2))))
      (let [f (fn ([_x] :one) ([_x _y & _more] :variadic))]
        (is (= :one (f 1)))
        (is (= :variadic (f 1 2)))
        (is (= :variadic (f 1 2 3))))
      (let [f (fn ([] :zero) ([& _xs] :variadic))]
        (is (= :zero (f)))
        (is (= :variadic (f 1)))))

    (testing "variadic arities collect the remaining arguments"
      (is (nil? ((fn [& xs] xs))))
      (is (= '(1) ((fn [& xs] xs) 1)))
      (is (= '(1 2 3) ((fn [& xs] xs) 1 2 3)))
      (is (= '(nil) ((fn [& xs] xs) nil)))
      (is (= [1 '(2 3)] ((fn [x & xs] [x xs]) 1 2 3)))
      (is (= [1 nil] ((fn [x & xs] [x xs]) 1)))
      (is (seq? ((fn [& xs] xs) 1)))
      (is (= 3 (count ((fn [& xs] xs) 1 2 3))))
      (is (= '(0 1 2 3 4) (apply (fn [& xs] xs) (range 5)))))

    (testing "a named function can refer to itself"
      (let [f (fn self [] self)]
        (is (= f (f))))
      (is (= 120 ((fn fact [n] (if (zero? n) 1 (* n (fact (dec n))))) 5)))
      (is (fn? ((fn fn-test-shadowed [] fn-test-shadowed))))
      (is (= :global fn-test-shadowed)))

    (testing "recur rebinds the parameters in tail position"
      (is (= :done ((fn [n] (if (zero? n) :done (recur (dec n)))) 3)))
      (is (= [0 3] ((fn [a b] (if (pos? a) (recur (dec a) (inc b)) [a b])) 3 0)))
      (is (= :done ((fn [n] (if (zero? n) :done (recur (dec n)))) 10000)))
      (is (= :done ((fn f ([n] (if (zero? n) :done (recur (dec n))))) 3)))
      (is (= :empty ((fn [& xs] (if (seq xs) (recur (rest xs)) :empty)) 1 2 3))))

    (testing "preconditions and postconditions"
      (is (= 2 ((fn [x] {:pre [(pos? x)] :post [(= 2 %)]} (inc x)) 1)))
      (is (= :ok ((fn [] {:post [(= :ok %)]} :ok))))
      (is (= 1 ((fn [a b] {:pre [(< a b)]} a) 1 2)))
      (is (p/thrown? ((fn [x] {:pre [(pos? x)]} x) -1)))
      (is (p/thrown? ((fn [x] {:post [(pos? %)]} x) -1))))

    (testing "calling with the wrong number of arguments throws"
      ;; ClojureScript emits no arity check for a function with only one
      ;; parameter list, so only the multi-arity dispatch is exercised there.
      #?(:cljs nil
         :default
         (do
           (is (p/thrown? ((fn [x] x))))
           (is (p/thrown? ((fn [x] x) 1 2)))
           (is (p/thrown? ((fn [_x & _xs] :ok))))))
      (is (p/thrown? ((fn ([_x] :one) ([_x _y] :two)) 1 2 3))))))
