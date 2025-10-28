(ns clojure.core-test.vec
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vec
  (deftest test-vec
    (testing "with nil"
      (is (= [] (vec nil))))

    (testing "with empty collections"
      (is (= [] (vec [])))
      (is (= [] (vec '())))
      (is (= [] (vec #{})))
      (is (= [] (vec {})))
      (is (= [] (vec ""))))

    (testing "with vectors"
      (is (= [1] (vec [1])))
      (is (= [1 2 3] (vec [1 2 3])))
      (is (= [1 2 3 4 5] (vec [1 2 3 4 5])))
      (is (vector? (vec [1 2 3]))))

    (testing "with lists"
      (is (= [1] (vec '(1))))
      (is (= [1 2 3] (vec '(1 2 3))))
      (is (= [1 2 3 4 5] (vec '(1 2 3 4 5)))))

    (testing "with sets"
      (is (= [1] (vec #{1})))
      (is (= 3 (count (vec #{1 2 3}))))
      (is (vector? (vec #{1 2 3}))))

    (testing "with maps"
      (is (= [[1 2]] (vec {1 2})))
      (is (= 2 (count (vec {1 2 3 4}))))
      (is (vector? (vec {1 2})))
      (let [result (vec {:a 1 :b 2})]
        (is (or (= [[:a 1] [:b 2]] result)
                (= [[:b 2] [:a 1]] result)))))

    (testing "with strings"
      (is (= [\a] (vec "a")))
      (is (= [\a \b \c] (vec "abc")))
      (is (= [\h \e \l \l \o] (vec "hello"))))

    (testing "with sequences"
      (is (= [0 1 2 3] (vec (range 4))))
      (is (= [0 2 4 6 8] (vec (filter even? (range 10)))))
      (is (= [1 2 3 4] (vec (map inc (range 4))))))

    (testing "preserves values with nil"
      (is (= [nil] (vec [nil])))
      (is (= [nil nil] (vec [nil nil])))
      (is (= [1 nil 3] (vec [1 nil 3])))
      (is (= [1 nil 3] (vec '(1 nil 3)))))

    (testing "with nested collections"
      (is (= [[1 2] [3 4]] (vec [[1 2] [3 4]])))
      (is (= [[1 2] [3 4]] (vec '([1 2] [3 4]))))
      (is (= [{:a 1} {:b 2}] (vec [{:a 1} {:b 2}]))))

    (testing "result is always a vector"
      (is (vector? (vec nil)))
      (is (vector? (vec [])))
      (is (vector? (vec '())))
      (is (vector? (vec #{})))
      (is (vector? (vec {})))
      (is (vector? (vec [1 2 3])))
      (is (vector? (vec '(1 2 3))))
      (is (vector? (vec #{1 2 3}))))

    (testing "with map entries"
      (let [entry (first {1 2})]
        (is (= [1 2] (vec entry)))))

    (testing "idempotent for vectors"
      (let [v [1 2 3]]
        (is (= v (vec v)))
        (is (= (vec v) (vec (vec v))))))

    (testing "with lazy sequences"
      (is (= [0 1 2 3 4] (vec (take 5 (range)))))
      (is (= [0 2 4 6 8] (vec (take 5 (filter even? (range)))))))

    (testing "with mixed types"
      (is (= [1 :a "b" nil true false] (vec [1 :a "b" nil true false])))
      (is (= [1 :a "b" nil true false] (vec '(1 :a "b" nil true false)))))

    (testing "with large collections"
      (let [large-list (range 1000)
            result (vec large-list)]
        (is (= 1000 (count result)))
        (is (vector? result))
        (is (= 0 (first result)))
        (is (= 999 (last result)))))

    (testing "with special numeric values"
      (is (= [##Inf] (vec [##Inf])))
      (is (= [##-Inf] (vec [##-Inf])))
      (let [result (vec [##NaN])]
        (is (vector? result))
        (is (= 1 (count result)))
        #?(:clj (is (Double/isNaN (first result))))
        #?(:cljs (is (js/isNaN (first result))))))

    (testing "equality"
      (is (= [1 2 3] (vec [1 2 3])))
      (is (= [1 2 3] (vec '(1 2 3))))
      (is (= (vec [1 2 3]) (vec '(1 2 3)))))

    (testing "with sorted collections"
      (is (= [1 2 3 4] (vec (sorted-set 1 2 3 4))))
      (is (= [[1 2] [3 4]] (vec (sorted-map 1 2 3 4)))))

    (testing "with keywords and symbols"
      (is (= [:a :b :c] (vec [:a :b :c])))
      (is (= ['a 'b 'c] (vec ['a 'b 'c])))
      (is (= [:ns/key] (vec [:ns/key])))
      (is (= ['ns/sym] (vec ['ns/sym]))))

    (testing "preserves order from sequences"
      (is (= [5 4 3 2 1] (vec (reverse [1 2 3 4 5])))))

    (testing "with boolean values"
      (is (= [true false] (vec [true false])))
      (is (= [true false true] (vec '(true false true)))))

    (testing "with empty string"
      (is (= [] (vec ""))))

    (testing "converts already-vector unchanged"
      (let [v [1 2 3]]
        (is (= v (vec v)))))

    #?(:cljs
       (testing "with JavaScript arrays"
         (is (= [1 2 3 4] (vec #js [1 2 3 4])))
         (is (vector? (vec #js [1 2 3 4])))))))
