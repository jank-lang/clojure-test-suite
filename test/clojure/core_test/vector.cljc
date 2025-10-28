(ns clojure.core-test.vector
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vector
  (deftest test-vector
    (testing "zero arguments"
      (is (= [] (vector)))
      (is (vector? (vector))))

    (testing "single argument"
      (is (= [1] (vector 1)))
      (is (= [nil] (vector nil)))
      (is (= [:a] (vector :a)))
      (is (= ["hello"] (vector "hello")))
      (is (= [[1 2 3]] (vector [1 2 3]))))

    (testing "multiple arguments"
      (is (= [1 2] (vector 1 2)))
      (is (= [1 2 3] (vector 1 2 3)))
      (is (= [1 2 3 4] (vector 1 2 3 4)))
      (is (= [1 2 3 4 5] (vector 1 2 3 4 5))))

    (testing "nil as argument"
      (is (= [nil] (vector nil)))
      (is (= [nil nil] (vector nil nil)))
      (is (= [1 nil 3] (vector 1 nil 3)))
      (is (= [nil 2 nil] (vector nil 2 nil))))

    (testing "mixed types"
      (is (= [1 :a "b" nil true false] (vector 1 :a "b" nil true false)))
      (is (= [{} [] #{}] (vector {} [] #{}))))

    (testing "collections as arguments"
      (is (= [[1 2] [3 4]] (vector [1 2] [3 4])))
      (is (= [{:a 1} {:b 2}] (vector {:a 1} {:b 2})))
      (is (= [#{1 2} #{3 4}] (vector #{1 2} #{3 4})))
      (is (= ['(1 2) '(3 4)] (vector '(1 2) '(3 4)))))

    (testing "result is a vector"
      (is (vector? (vector)))
      (is (vector? (vector 1)))
      (is (vector? (vector 1 2 3))))

    (testing "preserves order"
      (is (= [1 2 3 4 5] (vector 1 2 3 4 5)))
      (is (= [:z :y :x] (vector :z :y :x))))

    (testing "with many arguments"
      (let [many-args (repeat 100 :x)
            result (apply vector many-args)]
        (is (= 100 (count result)))
        (is (vector? result))
        (is (= :x (first result)))
        (is (= :x (last result)))))

    (testing "equality and identity"
      (is (= (vector 1 2 3) [1 2 3]))
      (is (not (identical? (vector 1 2 3) (vector 1 2 3))))
      (is (= (vector) []))
      (is (= (vector nil) [nil])))

    (testing "special values"
      (is (= [##Inf] (vector ##Inf)))
      (is (= [##-Inf] (vector ##-Inf)))
      (let [result (vector ##NaN)]
        (is (vector? result))
        (is (= 1 (count result)))
        #?(:clj (is (Double/isNaN (first result))))
        #?(:cljs (is (js/isNaN (first result))))))

    (testing "large numbers"
      (is (= [1000000000000] (vector 1000000000000)))
      (is (= [1N] (vector 1N)))
      #?(:clj (is (= [1.0M] (vector 1.0M)))))

    (testing "keywords and symbols"
      (is (= [:keyword] (vector :keyword)))
      (is (= [:namespaced/keyword] (vector :namespaced/keyword)))
      (is (= ['symbol] (vector 'symbol)))
      (is (= ['namespaced/symbol] (vector 'namespaced/symbol))))

    (testing "strings and characters"
      (is (= [""] (vector "")))
      (is (= ["hello world"] (vector "hello world")))
      #?(:clj (is (= [\a] (vector \a))))
      #?(:clj (is (= [\a \b \c] (vector \a \b \c)))))

    (testing "booleans"
      (is (= [true] (vector true)))
      (is (= [false] (vector false)))
      (is (= [true false] (vector true false))))

    (testing "nested vectors"
      (is (= [[]] (vector [])))
      (is (= [[[1]]] (vector [[1]])))
      (is (= [[1 2] [3 4] [5 6]] (vector [1 2] [3 4] [5 6]))))

    (testing "identical arguments"
      (is (= [1 1 1 1] (vector 1 1 1 1)))
      (is (= [:a :a :a] (vector :a :a :a)))
      (let [x {:a 1}]
        (is (= [x x] (vector x x)))
        (is (identical? x (first (vector x x))))))))
