(ns clojure.core-test.loop
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/loop

  ;; DOCSTRING:
  ;; clojure.core/loop
  ;; [bindings & body]
  ;; Evaluates the exprs in a lexical context in which the symbols in
  ;; the binding-forms are bound to their respective init-exprs or parts
  ;; therein. Acts as a recur target.

  (def loop-global :global-value)

  (deftest test-loop
    (testing "the body is evaluated in a lexical context of the bindings"
      (is (= 1 (loop [x 1] x)))
      (is (= [1 2] (loop [x 1 y 2] [x y])))
      (is (= [1 2 3] (loop [x 1 y 2 z 3] [x y z]))))

    (testing "the body is an implicit `do`"
      (is (= 3 (loop [x 1] x 2 3)))
      (is (= :last (loop [x 1] :first :last)))
      (is (nil? (loop [x 1]))))                 ; no body forms

    (testing "init-exprs are evaluated once, in order, and may use earlier bindings"
      (is (= [1 1] (loop [x 1 y x] [x y])))
      (is (= [1 2] (loop [x 1 y (inc x)] [x y])))
      (is (= [[1 2] 2] (loop [x 1 y [x (inc x)] z (count y)] [y z])))
      (is (= 6 (loop [a 1 b (+ a 1) c (+ b 2) d (+ c 2)] d)))
      (let [calls (volatile! 0)]
        (is (= 3 (loop [x (vswap! calls inc)] (if (< x 3) (recur (inc x)) x))))
        (is (= 1 @calls)))
      (let [order (volatile! [])]
        (loop [_a (vswap! order conj :a)
               _b (vswap! order conj :b)]
          (vswap! order conj :body))
        (is (= [:a :b :body] @order))))

    (testing "an empty binding vector is legal"
      (is (= 1 (loop [] 1)))
      (is (nil? (loop [])))
      (is (= 2 (loop [] 1 2))))

    (testing "the bindings are local to the loop body"
      (is (= [:inner :global-value]
             [(loop [loop-global :inner] loop-global) loop-global]))
      (is (= [2 1] (loop [x 1] [(loop [x 2] x) x])))
      (is (= [3 1] (loop [x 1] [(loop [y (+ x 2)] y) x]))))

    (testing "a binding may shadow an earlier binding of the same name"
      (is (= 2 (loop [x 1 x 2] x)))
      (is (= 2 (loop [x 1 x (inc x)] x)))
      (is (= :inner (loop [x :outer x :inner] x))))

    (testing "`loop` acts as a `recur` target"
      (is (= 3 (loop [x 0] (if (< x 3) (recur (inc x)) x))))
      (is (= :done (loop [x 0] (if (= x 3) :done (recur (inc x))))))
      (is (= [0 3] (loop [a 3 b 0] (if (pos? a) (recur (dec a) (inc b)) [a b]))))
      ;; the new values are evaluated against the old bindings and then
      ;; rebound simultaneously
      (is (= [2 1] (loop [a 1 b 2] (if (< a b) (recur b a) [a b]))))
      ;; a binding may be rebound to a value of a different type
      (is (= [:done "1"] (loop [x 1] (if (number? x) (recur (str x)) [:done x]))))
      (is (= :done (loop [x 1] (if x (recur nil) :done)))))

    (testing "closures capture the binding of the iteration in which they were made"
      (is (= 1 (let [f (loop [x 1] (fn [] x))] (f))))
      (is (= 2 (loop [x 0 f nil]
                 (if (< x 3)
                   (recur (inc x) (fn [] x))
                   (f)))))
      (is (= [1 3] (loop [x 1 f (fn [y] (+ x y))] [x (f 2)]))))

    (testing "binding forms may destructure the init-exprs"
      (is (= [1 2] (loop [[a b] [1 2]] [a b])))
      (is (= [3 2] (loop [[a b] [1 2]] (if (< a 3) (recur [(inc a) b]) [a b]))))
      (is (= 1 (loop [{:keys [a]} {:a 1}] a)))
      (is (= [2 4 6] (loop [[x & xs] [1 2 3] acc []]
                       (if x (recur xs (conj acc (* 2 x))) acc)))))

    (testing "nested loops each have their own `recur` target"
      (is (= [[0 1] [1 2]]
             (loop [i 0 acc []]
               (if (< i 2)
                 (recur (inc i)
                        (conj acc
                              [i (loop [j 0]
                                   (if (< j (inc i)) (recur (inc j)) j))]))
                 acc)))))

    (testing "`loop` does not force lazy sequences"
      (is (= '(0 1 2) (loop [s (range)] (take 3 s))))
      (is (= 1 (loop [s (map inc (range))] (first s))))
      (is (= [0 1 2] (loop [s (range) acc []]
                       (if (< (count acc) 3)
                         (recur (next s) (conj acc (first s)))
                         acc)))))

    ;; `loop` checks its binding vector and its `recur` forms as it expands,
    ;; and the CLJS test setup has no `eval`, so this is only tested where
    ;; `eval` is available.
    #?(:cljs nil
       :default
       (testing "invalid binding vectors and `recur` forms are rejected"
         (are [form] (p/thrown? (eval form))
           '(loop [x] x)                        ; odd number of binding forms
           '(loop [x 1 y] x)                    ; odd number of binding forms
           '(loop (x 1) x)                      ; the bindings must be a vector
           '(loop [1 2] 3)                      ; the binding target must be a symbol
           '(loop [:kw 1] :kw)                  ; keywords are not binding forms
           '(loop)                              ; no binding vector at all
           '(loop [x 0] (inc (recur (inc x))))  ; `recur` must be in tail position
           '(loop [x 0] (recur))                ; `recur` arity must match bindings
           '(loop [x 0] (recur 1 2))            ; `recur` arity must match bindings
           '(loop [] (recur 1)))))))            ; `recur` arity must match bindings
