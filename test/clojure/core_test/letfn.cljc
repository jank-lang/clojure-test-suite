(ns clojure.core-test.letfn
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists letfn

  ;; DOCSTRING:
  ;; clojure.core/letfn
  ;; ([fnspecs & body])
  ;; fnspec ==> (fname [params*] exprs) or (fname ([params*] exprs)+)
  ;;
  ;; Takes a vector of function specs and a body, and generates a set of
  ;; bindings of functions to their names. All of the names are available
  ;; in all of the definitions of the functions, as well as the body.

  (deftest test-letfn
    (testing "the names are bound to functions that can be called from the body"
      (is (= 2 (letfn [(inc* [x] (inc x))] (inc* 1))))
      (is (= [1 2] (letfn [(f [] 1) (g [] 2)] [(f) (g)]))))

    (testing "the body is an implicit `do`; its last expression is returned"
      (let [calls (volatile! [])]
        (is (= :last (letfn [(f [] (vswap! calls conj :f))]
                        (f)
                        (vswap! calls conj :body)
                        :last)))
        (is (= [:f :body] @calls))))

    (testing "a fnspec is either (fname [params*] exprs) or (fname ([params*] exprs)+)"
      (letfn [(f ([] :zero)
                 ([x] [:one x])
                 ([x y] [:two x y])
                 ([x y & more] [:more x y (vec more)]))]
        (is (= :zero (f)))
        (is (= [:one 1] (f 1)))
        (is (= [:two 1 2] (f 1 2)))
        (is (= [:more 1 2 [3 4]] (f 1 2 3 4)))))

    (testing "all of the names are available in all of the definitions"
      ;; mutual recursion
      (letfn [(even?* [n] (if (zero? n) true (odd?* (dec n))))
              (odd?* [n] (if (zero? n) false (even?* (dec n))))]
        (is (true? (even?* 10)))
        (is (false? (even?* 11)))
        (is (true? (odd?* 11)))
        (is (false? (odd?* 10))))
      ;; forward reference: an earlier fn calls a later one
      (letfn [(double-all [xs] (mapv twice xs))
              (twice [x] (* 2 x))]
        (is (= [2 4 6] (double-all [1 2 3])))))

    (testing "a function can call itself by name or with `recur`"
      (letfn [(fact [n] (if (zero? n) 1 (* n (fact (dec n)))))]
        (is (= 120 (fact 5))))
      (letfn [(sum-to [n acc] (if (zero? n) acc (recur (dec n) (+ acc n))))]
        (is (= 15 (sum-to 5 0)))))

    (testing "bindings shadow outer bindings and close over enclosing locals"
      (let [f (constantly :outer)
            y 10]
        (letfn [(f [] :inner)
                (add-y [x] (+ x y))]
          (is (= :inner (f)))
          (is (= 15 (add-y 5))))))

    (testing "the bound names are bound to ordinary functions"
      (letfn [(f [] 1)]
        (is (fn? f))
        (is (ifn? f)))
      ;; a function can escape its `letfn`
      (let [inc* (letfn [(inc* [x] (inc x))] inc*)]
        (is (= 2 (inc* 1))))
      ;; and it can return a function
      (is (= 6 ((letfn [(mk [n] (fn [x] (* n x)))] (mk 3)) 2))))

    (testing "letfns nest"
      (letfn [(double [x] (* 2 x))]
        (letfn [(quad [x] (double (double x)))]
          (is (= 8 (quad 2))))))

    (testing "empty fnspecs vectors and empty bodies"
      (is (= :body (letfn [] :body)))
      (is (nil? (letfn [(f [] 1)]))))))
