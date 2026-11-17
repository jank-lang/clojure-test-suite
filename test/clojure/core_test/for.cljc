(ns clojure.core-test.for
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/for

  ;; DOCSTRING:
  ;; clojure.core/for
  ;; [seq-exprs body-expr]
  ;; List comprehension. Takes a vector of one or more
  ;; binding-form/collection-expr pairs, each followed by zero or more
  ;; modifiers, and yields a lazy sequence of evaluations of expr.
  ;; Collections are iterated in a nested fashion, rightmost fastest,
  ;; and nested coll-exprs can refer to bindings created in prior
  ;; binding-forms.  Supported modifiers are: :let [binding-form expr ...],
  ;; :while test, :when test.
  ;; 
  ;; (take 100 (for [x (range 100000000) y (range 1000000) :while (< y x)] [x y]))

  (deftest test-for
    (testing "yields a lazy sequence of evaluations of the body"
      (let [s (for [x (range 5)] (* 2 x))]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= '(0 2 4 6 8) s)))
      (is (seq? (for [x [1 2]] x)))
      (is (sequential? (for [x [1 2]] x)))

      ;; the body is not evaluated until the sequence is realized
      (let [evals (atom 0)
            s (for [x [1 2 3]] (do (swap! evals inc) x))]
        (is (zero? @evals))
        (is (= [1 2 3] (vec s)))
        (is (= 3 @evals)))

      ;; because it is lazy it can be consumed from an infinite collection
      (is (= [0 1 4 9] (take 4 (for [x (range) :let [sq (* x x)]] sq)))))

    (testing "a single binding-form/collection-expr pair"
      (is (= [1 2 3] (for [x [1 2 3]] x)))
      (is (= [1 2 3] (for [x '(1 2 3)] x)))
      (is (= [0 1 2 3 4] (for [x (range 5)] x)))
      (is (= [2 4 6] (for [x [1 2 3]] (* 2 x))))
      ;; the body may evaluate to nil
      (is (= '(nil nil nil) (for [x [1 2 3]] nil))))

    (testing "the body is evaluated once for each element"
      (let [log (atom [])]
        (is (= [[1 :a] [1 :b] [2 :a] [2 :b]]
               (doall (for [x [1 2] y [:a :b]]
                        (let [pair [x y]]
                          (swap! log conj pair)
                          pair)))))
        (is (= [[1 :a] [1 :b] [2 :a] [2 :b]] @log))))

    (testing "collections are iterated in a nested fashion, rightmost fastest"
      (is (= [[1 :a] [1 :b] [2 :a] [2 :b] [3 :a] [3 :b]]
             (for [x [1 2 3] y [:a :b]] [x y])))
      (is (= [[0 0] [0 1] [1 0] [1 1]]
             (for [x [0 1] y [0 1]] [x y])))
      (is (= [[0 0 0] [0 0 1] [0 1 0] [0 1 1] [1 0 0] [1 0 1] [1 1 0] [1 1 1]]
             (for [x [0 1] y [0 1] z [0 1]] [x y z])))
      ;; the leftmost binding varies slowest
      (is (= [0 0 0 1 1 1 2 2 2] (for [x (range 3) y (range 3)] x))))

    (testing "nested collection expressions can refer to prior bindings"
      (is (= [[1 0] [2 0] [2 1] [3 0] [3 1] [3 2]]
             (for [x [1 2 3] y (range x)] [x y])))
      ;; each inner collection expression is evaluated once per outer element
      (let [evals (atom 0)]
        (is (= [1 2 3]
               (vec (for [x [1 2 3] y (do (swap! evals inc) [x])] y))))
        (is (= 3 @evals)))
      ;; an infinite outer collection with a finite inner collection
      ;; (an infinite inner collection would never terminate)
      (is (= [[0 0] [0 1] [0 2] [1 0]]
             (take 4 (for [x (range) y (range 3)] [x y])))))

    (testing "destructuring in binding-forms"
      (is (= [3 7] (for [[a b] [[1 2] [3 4]]] (+ a b))))
      (is (= [[1 2 3]] (for [[a [b c]] [[1 [2 3]]]] [a b c])))
      (is (= [[1 [2 3] [1 2 3]] [4 [5] [4 5]]]
             (for [[a & r :as all] [[1 2 3] [4 5]]] [a (vec r) all])))
      (is (= [[1 2]] (for [{:keys [a b]} [{:a 1 :b 2}]] [a b])))
      (is (= [[1 :d]] (for [{:keys [a b] :or {b :d}} [{:a 1}]] [a b])))
      (is (= #{[:a 1] [:b 2]} (set (for [[k v] {:a 1 :b 2}] [k v])))))

    (testing "a :when modifier filters the elements"
      (is (= [1 3 5] (for [x (range 6) :when (odd? x)] x)))
      (is (= [] (vec (for [x (range 5) :when false] x))))
      (is (= [] (vec (for [x (range 5) :when nil] x))))
      ;; anything truthy passes the test
      (is (= [0] (for [x [0 1 2] :when (when (zero? x) :yes)] x)))
      ;; several :when modifiers must all pass
      (is (= [7 9] (for [x (range 10) :when (odd? x) :when (> x 5)] x)))
      (is (= [] (vec (for [x (range 10) :when (odd? x) :when (even? x)] x))))
      ;; the filter is applied per combination in nested iteration
      (is (= [[0 1] [0 3] [1 0] [1 2]]
             (for [x [0 1] y (range 4) :when (odd? (+ x y))] [x y])))
      ;; filtering an infinite collection stays lazy
      (is (= [0 2 4] (take 3 (for [x (range) :when (even? x)] x)))))

    (testing "a :while modifier stops iterating"
      (is (= [0 1 2] (for [x (range 10) :while (< x 3)] x)))
      (is (= [] (vec (for [x [1 2 3] :while false] x))))
      (is (= [0] (for [x (range 5) :while (zero? x)] x)))
      ;; works on an infinite collection
      (is (= [0 1 2] (for [x (range) :while (< x 3)] x)))
      ;; it applies to the binding it follows: an inner :while ends only the
      ;; inner loop, so the outer loop continues and the inner collection
      ;; starts over for each new value of the outer binding
      (is (= [[1 0] [2 0] [2 1] [3 0] [3 1] [3 2]]
             (for [x [1 2 3] y (range) :while (< y x)] [x y])))
      ;; following the outermost binding it ends the whole comprehension
      (is (= [[0 0] [0 1] [1 0] [1 1]]
             (for [x (range) :while (< x 2) y [0 1]] [x y])))
      ;; the example from the docstring
      (is (= [[1 0] [2 0] [2 1] [3 0]]
             (take 4 (for [x (range 100000000)
                           y (range 1000000)
                           :while (< y x)]
                       [x y]))))
      ;; several :while modifiers must all hold, and the first one that fails
      ;; ends the loop
      (is (= [0 1 2] (for [x (range) :while (< x 10) :while (< x 3)] x)))
      (is (= [] (vec (for [x (range 10) :while (< x 5) :while (odd? x)] x)))))

    (testing "a :let modifier introduces bindings"
      (is (= [2 4 6] (for [x [1 2 3] :let [y (* 2 x)]] y)))
      ;; several bindings in one :let, each visible to the following ones
      (is (= [[1 2 3] [2 4 5]]
             (for [x [1 2] :let [y (* 2 x) z (+ y 1)]] [x y z])))
      ;; repeated :let modifiers, each visible to everything that follows
      (is (= [[1 2 3] [2 4 5]]
             (for [x [1 2] :let [y (* 2 x)] :let [z (+ y 1)]] [x y z])))
      ;; destructuring in a :let binding-form
      (is (= [3 7] (for [x [[1 2] [3 4]] :let [[a b] x]] (+ a b))))
      ;; a :let can refer to prior bindings, and later bindings can refer to it
      (is (= [[1 10 11] [2 20 22]]
             (for [x [1 2] y [(* 10 x)] :let [z (+ x y)]] [x y z])))
      (is (= [1 2 2 4] (for [x [1 2] :let [ys [x (* 2 x)]] y ys] y)))
      ;; the :let bindings are visible to :when, :while and the body
      (is (= [16 25 36 49 64 81]
             (for [x (range 10) :let [sq (* x x)] :when (> sq 10)] sq)))
      (is (= [0 1 4 9]
             (for [x (range) :let [sq (* x x)] :while (< sq 10)] sq))))

    (testing "modifiers can be mixed"
      ;; a single comprehension may carry different kinds of modifier
      (is (= [10 30 50] (for [x (range 6) :when (odd? x) :let [y (* 10 x)]] y)))
      (is (= [1 3 5] (for [x (range 10) :when (odd? x) :while (< x 6)] x)))
      (is (= [1 3 5] (for [x (range 10) :while (< x 6) :when (odd? x)] x)))
      ;; modifiers may be attached to different binding groups; each applies to
      ;; the binding it follows
      (is (= [[0 1] [0 3] [1 0] [1 2] [2 1] [2 3]]
             (for [x (range) :while (< x 3) y (range 4) :when (odd? (+ x y))]
               [x y])))
      (is (= [[1 10 1 11] [1 10 10 11] [2 20 2 22] [2 20 20 22]]
             (for [x [1 2] :let [y (* 10 x)] z [x y] :let [s (+ x y)]]
               [x y z s])))
      (is (= [[1 0] [3 0] [3 1] [3 2] [5 0] [5 1] [5 2] [5 3]]
             (for [x (range 6) :when (odd? x) y (range 4) :while (< y x)]
               [x y])))
      (is (= [[1 0] [2 0] [2 1]]
             (for [x (range) :while (< x 3) y (range) :while (< y x)] [x y]))))

    (testing "empty and nil collections yield an empty sequence"
      (are [result] (and (seq? result) (= () result) (empty? result) (zero? (count result)))
        (for [x []] x)
        (for [x nil] x)
        (for [x [1 2] y []] [x y])
        (for [x [1 2] y nil] [x y])))

    (testing "a modifier that rejects every element yields an empty sequence"
      (are [result] (and (seq? result) (= () result) (empty? result) (zero? (count result)))
        (for [x [1 2] :when false] x)
        (for [x [1 2] :when nil] x)
        (for [x [1 2] :while false] x)
        (for [x [1 2] :while nil] x)))

    (testing ":when tests every element, :while stops testing once one fails"
      ;; :when is invoked for every element, even when it rejects all of them
      (let [tests (atom 0)]
        (is (= () (for [x [1 2 3 4] :when (do (swap! tests inc) false)] x)))
        (is (= 4 @tests)))
      ;; :while is invoked once and the iteration ends immediately, so the
      ;; remaining elements are never tested
      (let [tests (atom 0)]
        (is (= () (for [x [1 2 3 4] :while (do (swap! tests inc) false)] x)))
        (is (= 1 @tests)))
      ;; with a test that passes at first, :when still visits every element
      (let [tests (atom 0)]
        (is (= [2 4] (for [x [1 2 3 4 5] :when (do (swap! tests inc) (even? x))] x)))
        (is (= 5 @tests)))
      ;; ... but :while stops at the first element that fails it, having tested
      ;; exactly one more element than it yielded
      (let [tests (atom 0)]
        (is (= [1 2] (for [x [1 2 3 4 5] :while (do (swap! tests inc) (< x 3))] x)))
        (is (= 3 @tests))))

    (testing "metadata of the elements is preserved"
      (let [item (with-meta [1 2] {:tag :item})]
        (is (= {:tag :item} (meta (first (for [x [item]] x)))))
        (is (= {:tag :item} (meta (first (for [x [item] :let [y x]] y)))))
        ;; a :when test may inspect the metadata of the bound element, which
        ;; also shows that the element still carries it when the test runs
        (is (= {:tag :item} (meta (first (for [x [item] :when (= :item (:tag (meta x)))] x)))))
        ;; the test decides which elements survive based on it
        (is (= [{:tag :item}]
               (map meta (for [x [item (with-meta [3 4] {:tag :other})]
                               :when (= :item (:tag (meta x)))]
                           x))))
        ;; a :let binding-form can also read the metadata and pass it along
        (is (= [:item] (for [x [item] :let [tag (:tag (meta x))]] tag)))
        ;; ... and a :while test can inspect it too, here to stop at the first
        ;; element whose metadata is not the expected one
        (is (= [item]
               (for [x [item (with-meta [3 4] {:tag :other})]
                     :while (= :item (:tag (meta x)))]
                 x)))))

    (testing "an exception thrown by a modifier propagates when realized"
      ;; the comprehension is lazy, so a modifier's exception is not thrown
      ;; where the comprehension is built, only once the failing element is
      ;; reached. A list (rather than a chunked range) is used so that exactly
      ;; the elements up to the failing one are realized.
      (let [s (for [x (list 0 1 2 3 4 5)
                    :when (if (= 3 x) (throw (ex-info "uh oh" {})) true)]
                x)]
        (is (seq? s))
        (is (= [0 1 2] (vec (take 3 s))))
        (is (p/thrown? (doall s))))
      (let [s (for [x (list 0 1 2 3 4 5)
                    :let [y (if (= 3 x) (throw (ex-info "uh oh" {})) x)]]
                y)]
        (is (seq? s))
        (is (= [0 1 2] (vec (take 3 s))))
        (is (p/thrown? (doall s))))
      (let [s (for [x (list 0 1 2 3 4 5)
                    :while (if (= 3 x) (throw (ex-info "uh oh" {})) true)]
                x)]
        (is (seq? s))
        (is (= [0 1 2] (vec (take 3 s))))
        (is (p/thrown? (doall s))))
      ;; the exception ends the whole comprehension, whichever nested binding
      ;; group the failing modifier belongs to
      (are [result] (p/thrown? (doall result))
        (for [x [0 1] y (range 4)
              :when (if (and (= x 1) (= y 2)) (throw (ex-info "uh oh" {})) true)]
          [x y])
        (for [x [0 1] y (range 4)
              :let [z (if (and (= x 1) (= y 2)) (throw (ex-info "uh oh" {})) y)]]
          [x z])
        (for [x [1 2 3] y (range 4)
              :while (if (and (= x 2) (= y 1)) (throw (ex-info "uh oh" {})) (< y x))]
          [x y])))

    (testing "collection expressions must be seqable"
      (is (p/thrown? (doall (for [x 5] x))))
      (is (p/thrown? (doall (for [x [1 2] y :a] [x y])))))))
