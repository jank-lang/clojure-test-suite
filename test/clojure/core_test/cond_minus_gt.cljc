(ns clojure.core-test.cond-minus-gt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/cond->

  ;; DOCSTRING:
  ;; clojure.core/cond->
  ;; [expr & clauses]
  ;; Takes an expression and a set of test/form pairs. Threads expr (via ->)
  ;; through each form for which the corresponding test
  ;; expression is true. Note that, unlike cond branching, cond-> threading does
  ;; not short circuit after the first true test expression.

  (deftest test-cond->
    (testing "with no clauses, expr is returned unchanged"
      (is (= 42 (cond-> 42)))
      (is (nil? (cond-> nil)))
      (is (= [1 2] (cond-> [1 2])))
      (is (= {:a 1} (cond-> {:a 1})))
      (is (= "abc" (cond-> "abc"))))

    (testing "expr is threaded through every form whose test is true"
      (is (= 2 (cond-> 1
                 true inc)))
      (is (= 3 (cond-> 1
                 true inc
                 true inc)))
      (is (= [1 2 3] (cond-> [1 2]
                       true (conj 3))))
      (is (= {:a 1 :b 2} (cond-> {:a 1}
                           true (assoc :b 2))))
      (is (= 2 (cond-> 1
                 (odd? 1) inc))))

    (testing "forms whose test is false or nil are skipped"
      (is (= 1 (cond-> 1
                 false inc)))
      (is (= 1 (cond-> 1
                 nil inc)))
      (is (= 1 (cond-> 1
                 (even? 1) inc)))
      (is (= 1 (cond-> 1
                 false inc
                 nil inc)))
      (is (= [1 2] (cond-> [1 2]
                     false (conj 3)
                     nil (conj 4)))))

    (testing "the selected forms are threaded in the order given"
      (is (= [:a :b] (cond-> []
                       true (conj :a)
                       true (conj :b))))
      (is (= [:b :a] (cond-> []
                       true (conj :b)
                       true (conj :a))))
      (is (= 8 (cond-> 1
                 true (+ 1)
                 true (* 2)
                 true (+ 4))))
      (is (= -10 (cond-> 1
                   true (+ 4)
                   true (* 2)
                   true (- 20)))))

    (testing "clauses use `->` semantics: expr is inserted as the second item"
      (is (= -8 (cond-> 2 true (- 10))))
      (is (= 3 (cond-> 5 true (- 2))))
      (is (= "abc" (cond-> "a" true (str "b" "c"))))
      (is (= 1 (cond-> {:a 1} true :a)))
      (is (= :one (cond-> 1 true {1 :one})))
      (is (= 1 (cond-> 1 true #{1 2}))))

    (testing "any value other than false or nil selects its form"
      (is (= 2 (cond-> 1 :a inc)))
      (is (= 2 (cond-> 1 0 inc)))
      (is (= 2 (cond-> 1 "" inc)))
      (is (= 2 (cond-> 1 [] inc))))

    (testing "does not short circuit after the first true test"
      (is (= 4 (cond-> 1
                 true inc
                 true inc
                 true inc)))
      (is (= 3 (cond-> 1
                 true inc
                 false inc
                 true inc)))
      (is (= [:a :b :c] (cond-> []
                          true (conj :a)
                          true (conj :b)
                          nil (conj :x)
                          true (conj :c)))))

    (testing "every test is evaluated exactly once, in order"
      (let [tests (atom [])]
        (is (= 3 (cond-> 1
                   (do (swap! tests conj :first) true) inc
                   (do (swap! tests conj :second) false) inc
                   (do (swap! tests conj :third) true) inc)))
        (is (= [:first :second :third] @tests))))

    (testing "the form of a false test is never evaluated"
      (let [forms (atom [])]
        (is (= 1 (cond-> 1
                   false ((fn [x] (swap! forms conj :first) x))
                   true identity
                   nil ((fn [x] (swap! forms conj :second) x)))))
        (is (= [] @forms))))

    (testing "expr is evaluated exactly once, however many tests are true"
      (let [evals (atom 0)]
        (is (= 3 (cond-> (do (swap! evals inc) 1)
                   true inc
                   true inc)))
        (is (= 1 @evals)))
      (let [evals (atom 0)]
        (is (= 1 (cond-> (do (swap! evals inc) 1)
                   false inc
                   nil inc)))
        (is (= 1 @evals))))

    (testing "cond-> works with lazy and infinite sequences"
      (is (= 1 (cond-> (map inc (range)) true first)))
      (is (= 2 (cond-> (range) true (nth 2))))
      (is (= 5 (cond-> (filter odd? (range 10)) true (nth 2))))
      (is (= [1 2 3] (cond-> (range 4) true rest true vec)))
      (is (= '(0 1 2) (cond-> (range) true (#(take 3 %)))))
      (is (= [0 1 2] (cond-> (range) false (#(take 5 %)) true (#(take 3 %)) true vec))))

    (testing "metadata on the threaded value is preserved"
      (let [m (with-meta [1 2] {:a 1})]
        (is (= {:a 1} (meta (cond-> m true identity))))
        (is (= {:a 1} (meta (cond-> m false identity))))
        (is (= {:a 1} (meta (cond-> m true (conj 3)))))))

    (testing "a nil expr is threaded like any other value"
      (is (nil? (cond-> nil)))
      (is (nil? (cond-> nil false inc)))
      (is (nil? (cond-> nil nil inc)))
      (is (true? (cond-> nil true nil?)))
      (is (= [nil] (cond-> [] true (conj nil)))))

    (testing "cond-> composes with `->` and with itself"
      (is (= 3 (cond-> (-> 1 inc) true inc)))
      (is (= 6 (-> (cond-> 1 true inc) (* 3))))
      (is (= 3 (cond-> (cond-> 1 true inc) true inc)))
      (is (= 4 (let [x 3] (cond-> x true inc)))))))
