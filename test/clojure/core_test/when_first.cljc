(ns clojure.core-test.when-first
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-first
  (deftest test-when-first
    (testing "binds first element and executes"
      (is (= 1 (when-first [x [1 2 3]] x)))
      (is (= :a (when-first [x [:a :b :c]] x))))

    (testing "returns nil for empty sequence"
      (is (nil? (when-first [x []] x)))
      (is (nil? (when-first [x []] :never-executed))))

    (testing "returns nil for nil"
      (is (nil? (when-first [x nil] x)))
      (is (nil? (when-first [x nil] :never-executed))))

    (testing "binding available in body"
      (is (= 10 (when-first [x [5 6 7]] (* x 2))))
      (is (= "HELLO" (when-first [s ["hello" "world"]] (clojure.string/upper-case s)))))

    (testing "multiple expressions in body"
      (is (= 6 (when-first [x [2 3 4]]
                 (* x 2)
                 (* x 3)))))

    (testing "with lists"
      (is (= 1 (when-first [x '(1 2 3)] x)))
      (is (nil? (when-first [x '()] x))))

    (testing "with lazy sequences"
      (is (= 0 (when-first [x (range 10)] x)))
      (is (= 1 (when-first [x (map inc [0 1 2])] x))))

    (testing "with strings (sequence of characters)"
      (is (= \h (when-first [c "hello"] c)))
      (is (nil? (when-first [c ""] c))))

    (testing "with sets (unpredictable order)"
      (let [result (when-first [x #{1 2 3}] x)]
        (is (contains? #{1 2 3} result))))

    (testing "nested when-first"
      (is (= 1 (when-first [x [1 2]]
                 (when-first [y [1 2]]
                   y))))
      (is (nil? (when-first [x [1 2]]
                  (when-first [y []]
                    y)))))

    (testing "first element can be nil"
      (is (nil? (when-first [x [nil 2 3]] x)))
      (is (nil? (when-first [x [nil]] x))))

    (testing "first element can be false"
      (is (= false (when-first [x [false true]] x))))

    (testing "side effects in body"
      (let [a (atom 0)]
        (when-first [x [5 6 7]]
          (swap! a + x))
        (is (= 5 @a)))
      (let [a (atom 0)]
        (when-first [x []]
          (swap! a inc))
        (is (= 0 @a))))

    (testing "returns last expression result"
      (is (= :last (when-first [x [1 2 3]]
                     :first
                     :second
                     :last))))

    (testing "with filter"
      (is (= 2 (when-first [x (filter even? [1 2 3 4])] x)))
      (is (nil? (when-first [x (filter even? [1 3 5])] x))))

    (testing "binding shadows outer scope"
      (let [x 10]
        (is (= 1 (when-first [x [1 2 3]] x)))
        (is (= 10 x))))

    (testing "evaluates sequence expression once"
      (let [counter (atom 0)]
        (when-first [x (do (swap! counter inc) [1 2 3])]
          :body)
        (is (= 1 @counter))))

    (testing "with map entries (as sequence)"
      (is (= [:a 1] (when-first [e {:a 1 :b 2}] e))))

    (testing "with cons"
      (is (= 0 (when-first [x (cons 0 [1 2 3])] x))))

    (testing "with rest of sequence ignored"
      (is (= 1 (when-first [x [1 2 3 4 5]] x)))
      (is (= :first (when-first [x [:first :second :third]] x))))))
