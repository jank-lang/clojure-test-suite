(ns clojure.core-test.take
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists take
  (deftest test-take
    (testing "Basics"
      (let [xs (take 5 (range 10))]
        ;; Basic lazy behavior
        (is (p/lazy-seq? xs))
        (is (not (realized? xs)))
        (is (= '(0 1 2 3 4) xs)))
      (is (= '() (take 0 (range 10))))  ; take zero
      (is (= '(0) (take 1 (range 10)))) ; take 1
      (is (= (range 0 5) (take 5 (range 10)))) ; take 5

      ;; take equal and greater than length of seq
      (is (= (range 10) (take 10 (range 10))))
      (is (= (range 10) (take 100 (range 10))))

      ;; Infinite `range` lazy seq
      (is (= (range 0 5) (take 5 (range))))

      ;; empty seqs
      (is (= '() (take 5 nil)))
      (is (= '() (take 5 '())))
      (is (= '() (take 5 []))))
    
    (testing "Transducer versions"

      (is (= [] (into [] (take 0) (range 10))))  ; take zero
      (is (= [0] (into [] (take 1) (range 10)))) ; take 1
      (is (= (vec (range 0 5)) (into [] (take 5) (range 10)))) ; take 5

      ;; take equal and greater than length of seq
      (is (= (vec (range 10)) (into [] (take 10) (range 10))))
      (is (= (vec (range 10)) (into [] (take 100) (range 10))))

      ;; we can take the first part of an infinite lazy seq
      (is (= (vec (range 0 5)) (into [] (take 5) (range))))

      ;; empty seqs
      (is (= [] (into [] (take 5) nil)))
      (is (= [] (into [] (take 5) '())))
      (is (= [] (into [] (take 5) []))))

    (testing "Negative tests"
      (is (p/thrown? (doall (take nil (range 0 10)))))
      (is (p/thrown? (into [] (take nil) (range 0 10)))))))
