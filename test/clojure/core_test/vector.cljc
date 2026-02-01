(ns clojure.core-test.vector
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vector
  (deftest test-vector
    (testing "empty vector"
      (is (= [] (vector)))
      (is (vector? (vector))))

    (testing "single element"
      (is (= [1] (vector 1)))
      (is (= [:a] (vector :a)))
      (is (= ["string"] (vector "string")))
      (is (= [nil] (vector nil)))
      (is (= [\a] (vector \a)))
      (is (= [true] (vector true)))
      (is (= [false] (vector false))))

    (testing "multiple elements"
      (is (= [1 2 3] (vector 1 2 3)))
      (is (= [:a :b :c] (vector :a :b :c)))
      (is (= ["zz" "a" "42"] (vector "zz" "a" "42"))))

    (testing "multiple data structures"
      (is (= [[1 2] [3 4]] (vector [1 2] [3 4])))
      (is (= ['(1 2) '(3 4)] (vector '(1 2) '(3 4))))
      (is (= [{:a 1} {:b 2}] (vector {:a 1} {:b 2})))
      (is (= [#{1 2} #{3 4}] (vector #{1 2} #{3 4})))
      (is (= [[]] (vector [])))
      (is (= [{}] (vector {})))
      (is (= [#{}] (vector #{}))))

    (testing "preserves duplicates"
      (is (= [1 1 1] (vector 1 1 1)))
      (is (= [:a :a] (vector :a :a)))
      (is (= [nil nil nil] (vector nil nil nil))))

    (testing "large vectors"
      (is (= (into [] (range 100)) (apply vector (range 100)))))

    (testing "different types together"
      (is (= [1 2.5 3N 4M] (vector 1 2.5 3N 4M)))
      (is (= [1 :a "b" \c true nil '() [] #{} {}]
             (vector 1 :a "b" \c true nil '() [] #{} {}))))

    (testing "variadic args"
      (is (= [1 2 3 4 5 6 7 8 9 10] (vector 1 2 3 4 5 6 7 8 9 10)))
      (is (= [1 2 3 4 5 6 nil] (vector 1 2 3 4 5 6 nil))))))

