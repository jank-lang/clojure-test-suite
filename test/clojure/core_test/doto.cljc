(ns clojure.core-test.doto
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/doto

  ;; DOCSTRING:
  ;; clojure.core/doto
  ;; [x & forms]
  ;; Evaluates x then calls all of the methods and functions with the
  ;; value of x supplied at the front of the given arguments.  The forms
  ;; are evaluated in order.  Returns x.
  ;; 
  ;; (doto (new java.util.HashMap) (.put "a" 1) (.put "b" 2))

  (deftest test-doto
    (testing "returns x, however many forms there are"
      (is (= 1 (doto 1)))
      (is (= 1 (doto 1 inc inc)))
      (is (= [] (doto [] (conj 1))))
      (is (nil? (doto nil)))
      (is (nil? (doto nil (str "a"))))
      ;; x itself, not a copy of it
      (let [obj #?(:cljs (js-obj) :default (Object.))]
        (is (identical? obj (doto obj (str "!"))))))

    (testing "x is supplied at the front of each form's arguments, in order"
      (let [seen   (atom [])
            record (fn [v & args] (swap! seen conj (apply list v args)))]
        (is (= 1 (doto 1 (record) (record 2) (record 2 3))))
        (is (= '((1) (1 2) (1 2 3)) @seen))))

    (testing "any callable works as a form, e.g. a keyword"
      (is (= {:a 1} (doto {:a 1} :a))))

    (testing "the side effects of the forms are visible"
      (let [a (atom [])]
        (is (identical? a (doto a (swap! conj 1) (swap! conj 2))))
        (is (= [1 2] @a)))
      ;; `object-array` and `aset` are Clojure functions, so mutation of a
      ;; shared, mutable object is exercised without implementation-specific
      ;; interop code.
      (let [a (object-array 3)]
        (is (identical? a (doto a (aset 0 :a) (aset 1 :b) (aset 2 :c))))
        (is (= [:a :b :c] (vec a)))))

    (testing "x is evaluated exactly once, before the forms are"
      (let [log (atom [])]
        (is (= 1 (doto (do (swap! log conj :x) 1)
                   (as-> v (do (swap! log conj :form) v))
                   (as-> v (do (swap! log conj :form) v)))))
        (is (= [:x :form :form] @log))))

    (testing "each form must be callable"
      (is (p/thrown? (doto 1 "not a function")))
      (is (p/thrown? (doto 1 42))))

    (testing "x is required"
      (is (p/thrown? (eval '(doto)))))

    (testing "an exception from a form propagates and stops later forms"
      (let [calls (atom [])]
        (is (p/thrown? (doto 1
                         (as-> v (do (swap! calls conj :first) v))
                         (as-> _ (throw (ex-info "boom" {})))
                         (as-> v (do (swap! calls conj :third) v)))))
        (is (= [:first] @calls))))

    (testing "doto nests with itself and with `->`"
      (is (= [] (doto (doto [] (conj 1)) (conj 2))))
      (is (= [2] (-> (doto [] (conj 1)) (conj 2))))
      (is (= 2 (doto (-> 1 inc) inc))))))
