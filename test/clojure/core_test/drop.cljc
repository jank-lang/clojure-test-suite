(ns clojure.core-test.drop
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists drop
  (deftest test-drop
    (is (= (range 0 10) (drop 0 (range 10)))) ; drop none
    (is (= (range 1 10) (drop 1 (range 0 10))))
    (is (= (range 5 10) (drop 5 (range 0 10))))
    (is (= '() (drop 10 (range 10))))   ; drop them all
    (is (= '() (drop 100 (range 10))))  ; drop more than all

    (is (= 5 (first (drop 5 (range))))) ; lazy version handles infinite `range`

    ;; Empty collections
    (is (= '() (drop 5 nil)))
    (is (= '() (drop 5 [])))
    (is (= '() (drop 5 '())))

    ;; Transducer version
    (is (= (vec (range 5 10)) (into [] (drop 5) (range 0 10))))

    ;; Note that we can drop from other types of collections, but
    ;; because they are not sequential, we don't know exactly what the
    ;; result will be. This tests that something truthy remained after
    ;; dropping one item and that `drop` didn't throw when given maps
    ;; or sets. We need `doall` here to force the realization of the
    ;; lazy seq created by `drop`.
    (is (doall (drop 1 {:a 1 :b 2 :c 3})))
    (is (doall (drop 1 #{:a :b :c})))

    (testing "Transducer variants"
      (is (= (range 0 10) (into [] (drop 0) (range 10)))) ; drop none
      (is (= (range 1 10) (into [] (drop 1) (range 0 10))))
      (is (= (range 5 10) (into [] (drop 5) (range 0 10))))
      (is (= [] (into [] (drop 10) (range 10))))   ; drop them all
      (is (= [] (into [] (drop 100) (range 10))))  ; drop more than all

      ;; Empty collections
      (is (= [] (into [] (drop 5) nil)))
      (is (= [] (into [] (drop 5) [])))
      (is (= [] (into [] (drop 5) '()))))

    (testing "Negative tests"
      (is (p/thrown? (doall (drop nil (range 0 10)))))
      (is (p/thrown? (into [] (drop nil) (range 0 10)))))
    ))
