(ns clojure.core-test.minus-gt-gt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/->>

  ;; DOCSTRING:
  ;; clojure.core/->>
  ;; [x & forms]
  ;; Threads the expr through the forms. Inserts x as the
  ;; last item in the first form, making a list of it if it is not a
  ;; list already. If there are more forms, inserts the first form as the
  ;; last item in second form, etc.

  (deftest test-->>
    (testing "with no forms, x is returned unchanged"
      (is (= 42 (->> 42)))
      (is (nil? (->> nil)))
      (is (= [1 2] (->> [1 2])))
      (is (= "abc" (->> "abc")))
      (is (= {:a 1} (->> {:a 1})))
      (is (= {:m 1} (meta (->> (with-meta [1 2] {:m 1}) identity)))))

    (testing "a non-list form is made into a list with x as its last item"
      (is (= 6 (->> 5 inc)))
      (is (true? (->> nil nil?)))
      (is (= 1 (->> {:a 1} :a)))
      (is (= :one (->> 1 {1 :one})))
      (is (= 1 (->> 1 #{1 2})))
      (is (= 20 (->> 1 [10 20 30])))
      (is (= 3 (->> 4 dec)))
      (is (= "1" (->> 1 (str)))))

    (testing "x is inserted as the last item, never as the second"
      (is (= 8 (->> 2 (- 10))))
      (is (= -3 (->> 5 (- 2))))
      (is (= 12 (->> 2 (* 3 2))))
      (is (= 9 (->> 3 (+ 1 2 3))))
      (is (= "bca" (->> "a" (str "b" "c"))))
      (is (= [1 2] (->> 2 (conj [1]))))
      (is (= [1 2 3 4] (->> 4 (vector 1 2 3))))
      (is (= '(0 1 2 3) (->> [2 3] (concat [0 1])))))

    (testing "the same form threads x to the front with `->` but to the back with `->>`"
      (is (= 3 (-> 5 (- 2))))
      (is (= -3 (->> 5 (- 2))))
      (is (= "abc" (-> "a" (str "b" "c"))))
      (is (= "bca" (->> "a" (str "b" "c")))))

    (testing "with more forms, each result is threaded as the last item into the next form"
      (is (= 12 (->> 5 (+ 1) (* 2))))
      (is (= 8 (->> 5 (+ 1) (* 2) (- 20))))
      (is (= 9 (->> [1 2 3] (map inc) (reduce +))))
      (is (= '(3 5) (->> [1 2 3 4] (filter even?) (map inc))))
      (is (= [0 1 2 3] (->> [1 2 3] (into [0]))))
      (is (= {:a 1 :b 2} (->> {:a 1} (merge {:b 2}))))
      (is (= "xyabc" (->> "abc" (str "x" "y")))))

    (testing "each form is evaluated exactly once, in order"
      (let [calls (atom [])]
        (is (= 3 (->> 1
                      ((fn [x] (swap! calls conj :first) (inc x)))
                      ((fn [x] (swap! calls conj :second) (inc x))))))
        (is (= [:first :second] @calls))))

    (testing "x is evaluated exactly once"
      (let [calls (atom 0)]
        (is (= 3 (->> (do (swap! calls inc) 1) inc inc)))
        (is (= 1 @calls))))

    (testing "->> works with lazy and infinite sequences"
      (is (= 0 (->> (range) first)))
      (is (= '(0 1 2 3 4) (->> (range) (take 5))))
      (is (= '(2 4 6) (->> (range) (map inc) (filter even?) (take 3))))
      (is (= 105 (->> (iterate inc 100) (drop 5) first)))
      (is (= '(0 2) (->> (range) (remove odd?) (take 2))))
      (is (= '(:x :x) (->> (repeat :x) (take 2)))))

    (testing "`->>` composes with `->` and with itself"
      (is (= 25 (->> (-> 1 inc (* 10)) (+ 5))))
      (is (= 18 (-> (->> [1 2 3] (map inc) (reduce +)) (* 2))))
      (is (= [2 3 4] (->> [1 2 3] (map inc) vec)))
      (is (= 4 (-> 5 inc (->> (- 10)))))
      (is (= 8 (->> (-> [1 2 3] (conj 4)) (filter even?) (map inc) (reduce +))))
      (is (= 6 (->> (->> 1 inc) (* 3))))
      (is (= 4 (let [x 3] (->> x inc)))))

    (testing "exceptions thrown by a threaded form propagate and stop the pipeline"
      (let [calls (atom [])]
        (is (p/thrown?
             (->> 1
                  ((fn [x] (swap! calls conj :first) x))
                  ((fn [_] (throw (ex-info "boom" {}))))
                  ((fn [x] (swap! calls conj :third) x)))))
        (is (= [:first] @calls))))

    (testing "a nil x is a legitimate threaded value"
      (is (nil? (->> nil identity)))
      (is (true? (->> nil nil?)))
      (is (= [nil] (->> nil (conj [])))))))
