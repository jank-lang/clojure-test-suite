(ns clojure.core-test.drop-last
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists drop-last
  (deftest test-drop-last
    ;; Docstring:
    ;; ([coll] [n coll])
    ;; Return a lazy sequence of all but the last n (default 1) items in coll

    (testing "arity 1"
      ;; Make sure `drop-last` returns a lazy seq
      (let [s (drop-last [1 2 3])]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= '(1 2) s)))

      ;; try different data types
      (are [expected coll] (= expected (drop-last coll))
        (range 0 4) (range 0 5)
        [1 2 3 4] [1 2 3 4 5]
        [1 2 3 4] (list 1 2 3 4 5)
        [\a \b \c \d] "abcde"
        [1 2 3 4] (int-array [1 2 3 4 5])
        [[:a 1] [:b 2] [:c 3]] (sorted-map :a 1 :b 2 :c 3 :d 4)
        [:a :b :c :d] (sorted-set :a :b :c :d :e)
        [] [1]
        [] (list 1)
        [] '()
        [] []
        [] ""
        [] {}
        [] #{}
        [] nil)

      ;; These are unordered collections, so we just test for the length
      (is (= 1 (count (drop-last {:a 1 :b 2}))))
      (is (= 4 (count (drop-last #{:a :b :c :d :e})))))

    (testing "arity 2"
      ;; Make sure `drop-last` returns a lazy seq
      (let [s (drop-last 2 [1 2 3])]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1] s)))

      (are [expected n coll] (= expected (drop-last n coll))
        (range 0 5) 0 (range 0 5)
        (range 0 4) 1 (range 0 5)
        (range 0 3) 2 (range 0 5)
        [1 2 3 4 5] 0 [1 2 3 4 5]
        [1 2 3 4] 1 [1 2 3 4 5]
        [1 2 3] 2 [1 2 3 4 5]
        [1 2 3 4 5] 0 (list 1 2 3 4 5)
        [1 2 3 4] 1 (list 1 2 3 4 5)
        [1 2 3] 2 (list 1 2 3 4 5)
        [\a \b \c \d \e] 0 "abcde"
        [\a \b \c \d] 1 "abcde"
        [\a \b \c] 2 "abcde"
        [1 2 3 4 5] 0 (int-array [1 2 3 4 5])
        [1 2 3 4] 1 (int-array [1 2 3 4 5])
        [1 2 3] 2 (int-array [1 2 3 4 5])
        [[:a 1] [:b 2] [:c 3] [:d 4]] 0 (sorted-map :a 1 :b 2 :c 3 :d 4)
        [[:a 1] [:b 2] [:c 3]] 1 (sorted-map :a 1 :b 2 :c 3 :d 4)
        [[:a 1] [:b 2]] 2 (sorted-map :a 1 :b 2 :c 3 :d 4)
        [:a :b :c :d :e] 0 (sorted-set :a :b :c :d :e)
        [:a :b :c :d] 1 (sorted-set :a :b :c :d :e)
        [:a :b :c] 2 (sorted-set :a :b :c :d :e)
        (range 0 5) -1 (range 0 5)      ; n < 0
        (range 0 5) -100 (range 0 5)
        [] 1 [1]                       ; n = count
        [] 100 [1]                     ; n > count
        [] 1 (list 1)
        [] 100 (list 1)
        [] 1 {:a 1}
        [] 100 {:a 1}
        [] 1 #{:a}
        [] 100 #{:a}
        [] 1 "a"
        [] 100 "a"
        [] 1 '()
        [] 100 '()
        [] 1 []
        [] 100 []
        [] 1 ""
        [] 100 ""
        [] 1 {}
        [] 100 {}
        [] 1 #{}
        [] 100 #{}
        [] 1 nil))

    (testing "negative tests"
      ;; Note: `doall` is required to realize the lazy sequence which
      ;; then forces it to use the bad arguments and then throw
      (is (p/thrown? (doall (drop-last nil (range 5)))))
      (is (p/thrown? (doall (drop-last :a (range 5)))))
      (is (p/thrown? (doall (drop-last :a))))
      (is (p/thrown? (doall (drop-last 1 :a)))))))
