(ns clojure.core-test.if-let
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/if-let

  ;; DOCSTRING:
  ;; clojure.core/if-let
  ;; [bindings then]
  ;; [bindings then else & oldform]
  ;; bindings => binding-form test
  ;; 
  ;; If test is true, evaluates then with binding-form bound to the value of 
  ;; test, if not, yields else

  (deftest test-if-let
    (testing "a truthy test runs `then` with the binding in scope"
      (is (= 1 (if-let [x 1] x :else)))
      (is (= :a (if-let [x :a] x :else)))
      (is (= "str" (if-let [x "str"] x :else)))
      (is (= [1 2] (if-let [x [1 2]] x :else)))
      (is (= 2 (if-let [x 1] (inc x) :else)))
      ;; the binding is visible inside a function created in `then`
      (is (= 1 ((if-let [x 1] (fn [] x) (fn [] :else))))))

    (testing "the binding is local to `then`"
      (is (= :inner (let [x :outer] (if-let [x :inner] x :else))))
      ;; the binding does not leak into `else`, which sees the outer binding
      (is (= :outer (let [x :outer] (if-let [x nil] :then x))))
      (is (= :outer (let [x :outer] (if-let [_ nil] :then x))))
      ;; nested `if-let`s: an inner `then` sees the outer bindings too
      (is (= [1 :inner-else]
             (if-let [x 1]
               (if-let [y nil] [x y] [x :inner-else])
               :else))))

    (testing "the test expression is evaluated exactly once"
      (let [calls (atom 0)]
        (is (= 1 (if-let [x (do (swap! calls inc) 1)] x :else)))
        (is (= 1 @calls)))
      (let [calls (atom 0)]
        (is (= :else (if-let [x (do (swap! calls inc) nil)] :then :else)))
        (is (= 1 @calls))))

    (testing "the binding form may destructure the test value"
      (is (= 1 (if-let [[x] [1 2]] x :else)))
      (is (= [1 2] (if-let [[a b] [1 2]] [a b] :else)))
      (is (= [1 '(2 3)] (if-let [[a & r] [1 2 3]] [a r] :else)))
      (is (= [1 [1 2]] (if-let [[a :as all] [1 2]] [a all] :else)))
      (is (= [1 2 3] (if-let [[a [b c]] [1 [2 3]]] [a b c] :else)))
      (is (= 1 (if-let [{:keys [a]} {:a 1}] a :else)))
      (is (= 1 (if-let [{a :a} {:a 1}] a :else))))

    (testing "the truth test is the test value, not the destructured bindings"
      ;; the empty vector is truthy, so `then` runs even though `x` is nil
      (is (nil? (if-let [[x] []] x :else)))
      (is (false? (if-let [[x] [false]] x :else)))
      ;; nil and false are falsey, so no destructuring happens at all
      (is (= :else (if-let [[x] nil] :then :else)))
      (is (= :else (if-let [[x] false] :then :else))))

    (testing "only the selected branch is evaluated"
      (is (= :then (if-let [x 1] :then (throw (ex-info "unreachable" {})))))
      (is (= :else (if-let [x nil] (throw (ex-info "unreachable" {})) :else)))
      ;; the test is always evaluated, and always before either branch
      (let [log (volatile! [])]
        (is (= :then
               (if-let [x (do (vswap! log conj :test) 1)]
                 (do (vswap! log conj :then) :then)
                 (do (vswap! log conj :else) :else))))
        (is (= [:test :then] @log))))

    (testing "`then` and `else` are each a single form"
      (is (= :b (if-let [x 1] (do :a :b) :else)))
      (is (= :b (if-let [x nil] :then (do :a :b)))))

    (testing "with no `else`, a falsey test yields nil"
      (is (nil? (if-let [x nil] :then)))
      (is (nil? (if-let [x false] :then)))
      (is (nil? (if-let [x nil] x)))
      (is (= :then (if-let [x 1] :then))))

    (testing "the test and the binding are not forced"
      (is (= '(0 1 2) (if-let [s (range)] (take 3 s) :else)))
      (is (= 0 (if-let [s (range)] (first s) :else))))

    ;; `if-let` checks its binding vector as it expands, and the CLJS test
    ;; setup has no `eval`, so this is only tested where `eval` is available.
    #?(:cljs nil
       :default
       (testing "invalid binding vectors are rejected"
         ;; `if-let` accepts exactly one binding pair, unlike `let`
         (are [form] (p/thrown? (eval form))
           '(if-let [] :then)         ; no binding forms at all
           '(if-let [x] :then)        ; odd number of forms in the vector
           '(if-let [x 1 y] :then)    ; odd number of forms in the vector
           '(if-let [x 1 y 2] :then)  ; more than one binding pair
           '(if-let (x 1) :then)      ; the bindings must be a vector
           '(if-let {:x 1} :then)     ; the bindings must be a vector
           '(if-let [1 1] :then)      ; the binding form must be a symbol
           '(if-let [:k 1] :then)     ; keywords are not binding forms
           '(if-let [x 1]))))))       ; missing `then`
