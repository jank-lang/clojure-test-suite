(ns clojure.core-test.some-minus-gt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/some->

  ;; DOCSTRING:
  ;; clojure.core/some->
  ;; [expr & forms]
  ;; When expr is not nil, threads it into the first form (via ->),
  ;; and when that result is not nil, through the next etc

  (deftest test-some->
    (testing "with no forms, expr is returned unchanged"
      (are [expected] (= expected (some-> expected))
        42
        [1 2]
        {:a 1}
        "abc"
        false
        nil
        ()))

    (testing "a non-nil expr is threaded into the first form"
      (is (= 6 (some-> 5 inc)))
      (is (= 2 (some-> 1 inc)))
      (is (= [1 2 3] (some-> [1 2] (conj 3))))
      (is (= {:a 1 :b 2} (some-> {:a 1} (assoc :b 2))))
      (is (= "abc" (some-> "a" (str "b" "c")))))

    (testing "each result is threaded into the next form, in order"
      (is (= 9 (some-> 5 (+ 1) (* 2) (- 3))))
      (is (= {:a 1 :b 2} (some-> {} (assoc :a 1) (assoc :b 2))))
      (is (= [2 3 4] (some-> [1 2 3] (conj 4) rest vec)))
      (is (= 5 (some-> [1 2] (conj 3) count (+ 2))))
      (is (= [:a :b] (some-> [] (conj :a) (conj :b)))))

    (testing "forms use `->` semantics: the threaded value is inserted as the second item"
      (is (= -8 (some-> 2 (- 10))))
      (is (= 3 (some-> 5 (- 2))))
      (is (= "abc" (some-> "a" (str "b" "c"))))
      (is (= 1 (some-> {:a 1} (:a))))
      (is (= true (some-> false (or true))))
      ;; a non-list form is made into a list, with the threaded value second
      (is (= 1 (some-> {:a 1} :a)))
      (is (= :one (some-> 1 {1 :one})))
      (is (= 1 (some-> 1 #{1 2})))
      (is (= 20 (some-> 1 [10 20 30]))))

    (testing "a nil expr short-circuits and returns nil"
      (is (nil? (some-> nil)))
      (is (nil? (some-> nil inc)))
      (is (nil? (some-> nil inc inc inc)))
      (is (nil? (some-> nil identity str)))
      (is (nil? (some-> nil (conj 1))))
      ;; `(not nil)` would be true, but nil is returned instead
      (is (nil? (some-> nil not)))
      (is (nil? (some-> nil nil?))))

    (testing "a nil intermediate result short-circuits and returns nil"
      (is (nil? (some-> {:a 1} :b)))
      (is (nil? (some-> {:a 1} :b inc)))
      (is (nil? (some-> {:a 1} :b inc inc)))
      (is (nil? (some-> {:a nil} :a str)))
      (is (nil? (some-> {:a 1} (dissoc :a) :a inc)))
      (is (nil? (some-> {} (get :a) inc))))

    (testing "short-circuiting on nil is the point of some->"
      ;; these chains would blow up if nil were threaded onward
      (is (= 1 (some-> {:a {:b 1}} :a :b)))
      (is (nil? (some-> {:a nil} :a :b)))
      (is (nil? (some-> {} :a :b)))
      (is (nil? (some-> {} :a :b inc inc)))
      (is (nil? (some-> [] first inc (* 2)))))

    (testing "any value other than nil keeps threading, including false"
      (is (false? (some-> false)))
      (is (false? (some-> false identity)))
      ;; `(nil? false)` is false, which is not nil, so `not` still runs
      (is (true? (some-> false nil? not)))
      ;; false is a member of the set, so the lookup yields false, not nil,
      ;; and threading continues
      (is (true? (some-> false #{false :a} not)))
      ;; 0 is not nil either
      (is (= 1 (some-> 0 inc)))
      (is (= 2 (some-> 1 (+ 1)))))

    (testing "expr is evaluated exactly once"
      (let [calls (atom 0)]
        (is (= 3 (some-> (do (swap! calls inc) 1)
                         inc
                         inc)))
        (is (= 1 @calls)))
      (let [calls (atom 0)]
        (is (nil? (some-> (do (swap! calls inc) 1)
                          ((constantly nil))
                          inc)))
        (is (= 1 @calls)))
      (let [calls (atom 0)]
        (is (nil? (some-> (do (swap! calls inc) nil)
                          inc)))
        (is (= 1 @calls))))

    (testing "each threaded form is evaluated exactly once, in order"
      (let [calls (atom [])]
        (is (= 3 (some-> 1
                         ((fn [x] (swap! calls conj :first) (inc x)))
                         ((fn [x] (swap! calls conj :second) (inc x))))))
        (is (= [:first :second] @calls))))

    (testing "forms after a nil result are never evaluated"
      (let [calls (atom [])]
        (is (nil? (some-> nil
                          ((fn [x] (swap! calls conj :first) x))
                          ((fn [x] (swap! calls conj :second) x)))))
        (is (= [] @calls)))
      (let [calls (atom [])]
        (is (nil? (some-> {:a 1}
                          :b
                          ((fn [x] (swap! calls conj :second) x))
                          ((fn [x] (swap! calls conj :third) x)))))
        (is (= [] @calls)))
      (let [calls (atom [])]
        (is (nil? (some-> 1
                          ((fn [x] (swap! calls conj :first) (inc x)))
                          ((fn [x] (swap! calls conj :second) nil))
                          ((fn [x] (swap! calls conj :third) x))
                          ((fn [x] (swap! calls conj :fourth) x)))))
        (is (= [:first :second] @calls))))

    (testing "an exception from a threaded form propagates"
      (is (p/thrown? (some-> [1 2] (nth 5))))
      (is (p/thrown? (some-> {:a [1 2]} :a (nth 5) inc)))
      ;; a nil short-circuit means the throwing form is never reached
      (is (nil? (some-> nil (nth 5)))))

    (testing "some-> works with lazy and infinite sequences"
      (is (= 1 (some-> (map inc (range)) first)))
      (is (= 2 (some-> (range) (nth 2))))
      (is (= 5 (some-> (filter odd? (range 10)) (nth 2))))
      (is (= 7 (some-> (filter odd? (range)) (nth 3))))
      (is (= 5 (some-> (iterate inc 0) (nth 5))))
      (is (= [1 2 3] (some-> (range 4) rest vec)))
      (is (= '(0 1 2) (some-> (range) (#(take 3 %)))))
      (is (= [0 1 2] (some-> (range) (#(take 5 %)) (#(take 3 %)) vec))))

    (testing "metadata on the threaded value is preserved"
      (let [m (with-meta [1 2] {:a 1})]
        (is (= {:a 1} (meta (some-> m))))
        (is (= {:a 1} (meta (some-> m identity))))
        (is (= {:a 1} (meta (some-> m (conj 3)))))
        (is (= {:a 1} (meta (some-> m (conj 3) identity))))
        ;; a short-circuit yields nil, so there is no metadata to preserve
        (is (nil? (some-> m ((fn [_] nil)) identity)))))

    (testing "some-> composes with other threading macros and with itself"
      (is (= 3 (some-> (-> 1 inc) inc)))
      (is (= 6 (-> (some-> 1 inc) (* 3))))
      (is (= 3 (some-> (some-> 1 inc) inc)))
      (is (nil? (some-> (some-> nil inc) inc)))
      (is (= 25 (some->> (some-> 1 inc (* 10)) (+ 5))))
      (is (= [2 3 4] (some-> [1 2 3] (some->> (map inc) vec))))
      (is (= 4 (let [x 3] (some-> x inc)))))))