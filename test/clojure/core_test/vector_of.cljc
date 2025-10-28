(ns clojure.core-test.vector-of
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vector-of
  #?(:clj
     (deftest test-vector-of
       (testing "empty vectors with valid types"
         (is (empty? (vector-of :boolean)))
         (is (empty? (vector-of :byte)))
         (is (empty? (vector-of :short)))
         (is (empty? (vector-of :int)))
         (is (empty? (vector-of :long)))
         (is (empty? (vector-of :float)))
         (is (empty? (vector-of :double)))
         (is (empty? (vector-of :char))))

       (testing "result is a vector"
         (is (vector? (vector-of :int)))
         (is (vector? (vector-of :int 1 2 3)))
         (is (vector? (vector-of :long)))
         (is (vector? (vector-of :double 1.0 2.0))))

       (testing "with single value"
         (is (= [1] (vector-of :int 1)))
         (is (= [1] (vector-of :long 1)))
         (is (= [1.0] (vector-of :float 1.0)))
         (is (= [1.0] (vector-of :double 1.0)))
         (is (= [true] (vector-of :boolean true)))
         (is (= [false] (vector-of :boolean false))))

       (testing "with multiple values"
         (is (= [1 2] (vector-of :int 1 2)))
         (is (= [1 2 3] (vector-of :int 1 2 3)))
         (is (= [1 2 3 4] (vector-of :int 1 2 3 4)))
         (is (= [1 2 3 4 5] (vector-of :int 1 2 3 4 5)))
         (is (= [1 2 3 4 5 6] (vector-of :int 1 2 3 4 5 6))))

       (testing "coercion to target type"
         (is (= [1 2 3] (vector-of :int 1M 2.0 3.1)))
         (is (= [1.0 2.0 3.0] (vector-of :float 1 2 3)))
         (is (= [1.0 2.0 3.0] (vector-of :double 1 2 3))))

       (testing "with characters"
         (is (= [97 98 99] (vector-of :int \a \b \c)))
         (is (= [\a \b \c] (vector-of :char \a \b \c))))

       (testing "with many elements"
         (let [result (apply vector-of :int (range 1000))]
           (is (= 1000 (count result)))
           (is (vector? result))
           (is (= 0 (first result)))
           (is (= 999 (last result)))
           (is (= (vec (range 1000)) result))))

       (testing "equality with regular vectors"
         (is (= [1 2 3] (vector-of :int 1 2 3)))
         (is (= [1.0 2.0 3.0] (vector-of :double 1.0 2.0 3.0))))

       (testing "hash equality"
         (is (= (hash [1 2 3]) (hash (vector-of :int 1 2 3))))
         (is (= (hash [1.0 2.0]) (hash (vector-of :double 1.0 2.0)))))

       (testing "conj operation"
         (is (= [1 2 3 4] (conj (vector-of :int 1 2 3) 4)))
         (is (= [1.0 2.0 3.0] (conj (vector-of :double 1.0 2.0) 3.0))))

       (testing "into operation"
         (is (= [1 2 3 4 5] (into (vector-of :int) [1 2 3 4 5])))
         (is (= [1 2 3] (into (vector-of :int 1 2) [3]))))

       (testing "nth and get"
         (let [v (vector-of :int 10 20 30)]
           (is (= 10 (nth v 0)))
           (is (= 20 (nth v 1)))
           (is (= 30 (nth v 2)))
           (is (= 10 (get v 0)))
           (is (= 20 (get v 1)))
           (is (= 30 (get v 2)))))

       (testing "assoc operation"
         (is (= [1 99 3] (assoc (vector-of :int 1 2 3) 1 99)))
         (is (= [1.0 99.0 3.0] (assoc (vector-of :double 1.0 2.0 3.0) 1 99.0))))

       (testing "pop operation"
         (is (= [1 2] (pop (vector-of :int 1 2 3))))
         (is (= [1.0] (pop (vector-of :double 1.0 2.0)))))

       (testing "count operation"
         (is (= 0 (count (vector-of :int))))
         (is (= 3 (count (vector-of :int 1 2 3))))
         (is (= 5 (count (vector-of :long 1 2 3 4 5)))))

       (testing "seq operations"
         (is (nil? (seq (vector-of :int))))
         (is (= '(1 2 3) (seq (vector-of :int 1 2 3))))
         (is (= 1 (first (vector-of :int 1 2 3))))
         (is (= 3 (last (vector-of :int 1 2 3)))))

       (testing "with boolean type"
         (is (= [true false true] (vector-of :boolean true false true)))
         (is (= [false] (vector-of :boolean false))))

       (testing "with byte type"
         (is (= [1 2 3] (vector-of :byte 1 2 3)))
         (is (= [127] (vector-of :byte 127))))

       (testing "with short type"
         (is (= [1 2 3] (vector-of :short 1 2 3)))
         (is (= [1000] (vector-of :short 1000))))

       (testing "with long type"
         (is (= [1000000000000] (vector-of :long 1000000000000)))
         (is (= [1 2 3] (vector-of :long 1 2 3))))

       (testing "invalid type arguments throw exception"
         (is (thrown? IllegalArgumentException (vector-of nil)))
         (is (thrown? IllegalArgumentException (vector-of Float/TYPE)))
         (is (thrown? IllegalArgumentException (vector-of 'int)))
         (is (thrown? IllegalArgumentException (vector-of :integer)))
         (is (thrown? IllegalArgumentException (vector-of "")))
         (is (thrown? IllegalArgumentException (vector-of "int"))))

       (testing "nil values throw exception"
         (is (thrown? NullPointerException (vector-of :int nil)))
         (is (thrown? NullPointerException (vector-of :int 1 nil)))
         (is (thrown? NullPointerException (vector-of :int 1 2 nil)))
         (is (thrown? NullPointerException (vector-of :int 1 2 3 nil)))
         (is (thrown? NullPointerException (vector-of :long nil)))
         (is (thrown? NullPointerException (vector-of :double nil))))

       (testing "unsupported value types throw exception"
         (is (thrown? ClassCastException (vector-of :int true)))
         (is (thrown? ClassCastException (vector-of :int 1 2 3 4 5 false)))
         (is (thrown? ClassCastException (vector-of :int {:a 1 :b 2})))
         (is (thrown? ClassCastException (vector-of :int [1 2 3 4] [5 6])))
         (is (thrown? ClassCastException (vector-of :int '(1 2 3 4))))
         (is (thrown? ClassCastException (vector-of :int #{1 2 3 4})))
         (is (thrown? ClassCastException (vector-of :int (sorted-set 1 2 3 4))))
         (is (thrown? ClassCastException (vector-of :int 1 2 "3")))
         (is (thrown? ClassCastException (vector-of :int "1" "2" "3"))))

       (testing "empty check"
         (is (empty? (vector-of :int)))
         (is (not (empty? (vector-of :int 1)))))

       (testing "reduce operation"
         (is (= 6 (reduce + (vector-of :int 1 2 3))))
         (is (= 15 (reduce + 0 (vector-of :long 1 2 3 4 5)))))

       (testing "map over vector-of"
         (is (= [2 4 6] (mapv inc (vector-of :int 1 3 5))))
         (is (= [2.0 4.0 6.0] (mapv inc (vector-of :double 1.0 3.0 5.0)))))

       (testing "filter over vector-of"
         (is (= [2 4] (filterv even? (vector-of :int 1 2 3 4 5))))
         (is (= [1 3 5] (filterv odd? (vector-of :int 1 2 3 4 5)))))

       (testing "subvec operation"
         (is (= [2 3] (subvec (vector-of :int 1 2 3 4) 1 3)))
         (is (= [1.0 2.0] (subvec (vector-of :double 1.0 2.0 3.0) 0 2))))

       (testing "rseq operation"
         (is (= [3 2 1] (rseq (vector-of :int 1 2 3))))
         (is (nil? (rseq (vector-of :int)))))

       (testing "equality across different primitive types"
         (is (= (vector-of :int 1 2 3) (vector-of :long 1 2 3)))
         (is (= (vector-of :int 1 2 3) [1 2 3])))

       (testing "vector-of with zero values"
         (is (= [0 0 0] (vector-of :int 0 0 0)))
         (is (= [0.0 0.0] (vector-of :double 0.0 0.0))))

       (testing "vector-of with negative values"
         (is (= [-1 -2 -3] (vector-of :int -1 -2 -3)))
         (is (= [-1.0 -2.0] (vector-of :double -1.0 -2.0)))))

     :cljs
     (deftest test-vector-of
       (testing "vector-of not available in ClojureScript"
         (is true)))))
