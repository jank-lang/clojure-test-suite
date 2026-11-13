(ns clojure.core-test.mapv
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/mapv

  ;; DOCSTRING:
  ;; clojure.core/mapv
  ;; [f coll]
  ;; [f c1 c2]
  ;; [f c1 c2 c3]
  ;; [f c1 c2 c3 & colls]
  ;; Returns a vector consisting of the result of applying f to the
  ;; set of first items of each coll, followed by applying f to the set
  ;; of second items in each coll, until any one of the colls is
  ;; exhausted.  Any remaining items in other colls are ignored. Function
  ;; f should accept number-of-colls arguments.

  (deftest test-mapv
    (testing "returns a vector"
      (is (vector? (mapv inc [1 2 3])))
      (is (vector? (mapv inc [])))
      (is (vector? (mapv + [1 2] [3 4])))
      (is (vector? (mapv + [1 2] [3 4] [5 6])))
      (is (vector? (mapv + [1 2] [3 4] [5 6] [7 8])))
      (is (not (p/lazy-seq? (mapv inc [1 2 3])))))

    (testing "result is realized eagerly"
      ;; Unlike `map`, `mapv` calls `f` on every item before returning
      (let [calls (atom 0)
            result (mapv (fn [x] (swap! calls inc) (inc x)) (range 5))]
        (is (= 5 @calls))
        (is (= [1 2 3 4 5] result))))

    (testing "arity 2: [f coll]"
      (are [expected f coll] (= expected (mapv f coll))
        [1 2 3 4 5] inc (range 5)
        [2 3 4] inc [1 2 3]
        [2 3 4] inc '(1 2 3)
        [1 2 3] identity (map identity (range 1 4))
        [] identity []
        [] identity '()
        [] identity nil
        [] inc (range 0)
        (vec "abc") identity "abc")

      (is (= [1 4 9] (mapv #(* % %) [1 2 3])))
      (is (= [nil nil nil] (mapv (constantly nil) [1 2 3])))
      (is (= [true false true false] (mapv even? (range 4))))

      ;; Items pass through unchanged and metadata is preserved
      (let [item (with-meta [1 2] {:m 1})]
        (is (= {:m 1} (meta (first (mapv identity (list item))))))))

    (testing "arity 3: [f c1 c2]"
      (is (= [0 2 4 6 8] (mapv + (range 5) (range 5))))
      (is (= [[0 :a] [1 :b] [2 :c]] (mapv vector [0 1 2] [:a :b :c])))
      (is (= [3 5 7] (mapv + '(1 2 3) [2 3 4])))

      ;; mapv stops as soon as any one coll is exhausted
      (is (= [0 2 4 6] (mapv + (range 5) (range 4))))
      (is (= [0 2 4 6] (mapv + (range 4) (range 5))))

      ;; An empty or nil coll yields an empty vector
      (is (= [] (mapv + [] (range 5))))
      (is (= [] (mapv + (range 5) [])))
      (is (= [] (mapv + nil (range 5))))
      (is (= [] (mapv + (range 5) nil)))
      (is (= [] (mapv + nil nil))))

    (testing "arity 4: [f c1 c2 c3]"
      (is (= [0 3 6 9 12] (mapv + (range 5) (range 5) (range 5))))
      (is (= [6 9 12] (mapv + [1 2 3] [2 3 4] [3 4 5])))

      ;; mapv stops as soon as any one coll is exhausted
      (is (= [0 3 6 9] (mapv + (range 5) (range 5) (range 4))))
      (is (= [0 3 6 9] (mapv + (range 5) (range 4) (range 5))))
      (is (= [0 3 6 9] (mapv + (range 4) (range 5) (range 5))))

      ;; An empty or nil coll yields an empty vector
      (is (= [] (mapv + [] (range 5) (range 5))))
      (is (= [] (mapv + (range 5) [] (range 5))))
      (is (= [] (mapv + (range 5) (range 5) [])))
      (is (= [] (mapv + nil (range 5) (range 5))))
      (is (= [] (mapv + (range 5) nil (range 5))))
      (is (= [] (mapv + (range 5) (range 5) nil))))

    (testing "arity 5+: [f c1 c2 c3 & colls]"
      (is (= [0 4 8 12 16] (mapv + (range 5) (range 5) (range 5) (range 5))))
      (is (= [0 5 10 15 20]
             (mapv + (range 5) (range 5) (range 5) (range 5) (range 5))))

      ;; mapv stops as soon as any one coll is exhausted
      (is (= [0 4 8 12] (mapv + (range 5) (range 5) (range 5) (range 4))))
      (is (= [0 4 8 12] (mapv + (range 5) (range 5) (range 4) (range 5))))
      (is (= [0 4 8 12] (mapv + (range 5) (range 4) (range 5) (range 5))))
      (is (= [0 4 8 12] (mapv + (range 4) (range 5) (range 5) (range 5))))
      (is (= [0 5 10]
             (mapv + (range 5) (range 4) (range 5) (range 5) (range 3))))

      ;; An empty or nil coll yields an empty vector
      (is (= [] (mapv + nil (range 5) (range 5) (range 5))))
      (is (= [] (mapv + (range 5) nil (range 5) (range 5))))
      (is (= [] (mapv + (range 5) (range 5) nil (range 5))))
      (is (= [] (mapv + (range 5) (range 5) (range 5) nil)))
      (is (= [] (mapv + '() (range 5) (range 5) (range 5)))))

    (testing "negative cases"
      ;; `mapv` realizes eagerly, so the exception is thrown here without
      ;; needing to force the result the way `map` does
      (are [x] (p/thrown? (mapv identity x))
        1
        3.14
        true
        false
        :a
        'a)
      ;; Chars aren't seqs, except in CLJS where a char is a
      ;; one-character string
      #?(:cljs nil :default (is (p/thrown? (mapv identity \a)))))))
