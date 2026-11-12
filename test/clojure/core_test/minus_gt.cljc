(ns clojure.core-test.minus-gt
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/->

  ;; DOCSTRING:
  ;; clojure.core/->
  ;; [x & forms]
  ;; Threads the expr through the forms. Inserts x as the
  ;; second item in the first form, making a list of it if it is not a
  ;; list already. If there are more forms, inserts the first form as the
  ;; second item in second form, etc.

  (deftest test-->
    (testing "with no forms, x is returned unchanged"
      (is (= 42 (-> 42)))
      (is (nil? (-> nil)))
      (is (= [1 2] (-> [1 2])))
      (is (= "abc" (-> "abc")))
      (is (= {:a 1} (-> {:a 1}))))

    (testing "a non-list form is made into a list with x as its second item"
      (is (= 6 (-> 5 inc)))
      (is (= true (-> nil nil?)))
      (is (= 1 (-> {:a 1} :a)))
      (is (= :one (-> 1 {1 :one})))
      (is (= 1 (-> 1 #{1 2})))
      (is (= 20 (-> 1 [10 20 30]))))

    (testing "x is inserted as the second item, never as the last"
      (is (= -8 (-> 2 (- 10))))
      (is (= 3 (-> 5 (- 2))))
      (is (= "abc" (-> "a" (str "b" "c"))))
      (is (= [1 2 3] (-> [1 2] (conj 3))))
      (is (= 1 (-> {:a 1} (:a)))))

    (testing "with more forms, each result is threaded into the next form"
      (is (= 9 (-> 5 (+ 1) (* 2) (- 3))))
      (is (= {:a 1 :b 2} (-> {} (assoc :a 1) (assoc :b 2))))
      (is (= [2 3 4] (-> [1 2 3] (conj 4) rest vec)))
      (is (= 5 (-> [1 2] (conj 3) count (+ 2)))))

    (testing "each form is evaluated exactly once, in order"
      (let [calls (atom [])]
        (is (= 3 (-> 1
                     ((fn [x] (swap! calls conj :first) (inc x)))
                     ((fn [x] (swap! calls conj :second) (inc x))))))
        (is (= [:first :second] @calls))))

    (testing "-> works with lazy and infinite sequences"
      (is (= 1 (-> (map inc (range)) first)))
      (is (= 2 (-> (range) (nth 2))))
      (is (= 5 (-> (filter odd? (range 10)) (nth 2))))
      (is (= 5 (-> (iterate inc 0) (nth 5))))
      (is (= [1 2 3] (-> (range 4) rest vec))))

    (testing "-> composes with other code and with itself"
      (is (= 6 (-> (-> 1 inc) (* 3))))
      (is (= 4 (let [x 3] (-> x inc)))))

    (testing "`->` composes with `->>`"
      ;; the result of a `->` pipeline is the initial value of a `->>` pipeline
      (is (= 25 (->> (-> 1 inc (* 10)) (+ 5))))
      ;; the result of a `->>` pipeline is the initial value of a `->` pipeline
      (is (= 18 (-> (->> [1 2 3] (map inc) (reduce +)) (* 2))))
      ;; a `->>` form nested in a `->` pipeline still gets its argument last
      (is (= [2 3 4] (-> [1 2 3] (->> (map inc) vec))))
      ;; a `->` result nested in a `->>` pipeline still gets inserted second
      (is (= 4 (-> 5 inc (->> (- 10)))))
      ;; a longer pipeline can switch direction at any stage
      (is (= 8 (->> (-> [1 2 3] (conj 4)) (filter even?) (map inc) (reduce +)))))))
