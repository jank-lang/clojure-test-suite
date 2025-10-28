(ns clojure.core-test.when-some
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-some
  (deftest test-when-some
    (testing "executes when binding is not nil"
      (is (= 42 (when-some [x 42] x)))
      (is (= :yes (when-some [x true] :yes)))
      (is (= false (when-some [x false] x))))

    (testing "returns nil when binding is nil"
      (is (nil? (when-some [x nil] x)))
      (is (nil? (when-some [x nil] :never-executed))))

    (testing "false is not nil, executes body"
      (is (= false (when-some [x false] x)))
      (is (= :executed (when-some [x false] :executed))))

    (testing "zero is not nil, executes body"
      (is (= 0 (when-some [x 0] x))))

    (testing "empty collections are not nil"
      (is (= [] (when-some [v []] v)))
      (is (= {} (when-some [m {}] m)))
      (is (= #{} (when-some [s #{}] s)))
      (is (= "" (when-some [s ""] s))))

    (testing "binding available in body"
      (is (= 10 (when-some [x 5] (* x 2))))
      (is (= "hello" (when-some [s "hello"] s))))

    (testing "multiple expressions in body"
      (is (= 6 (when-some [x 2]
                 (* x 2)
                 (* x 3)))))

    (testing "with seq function"
      (is (= '(1 2 3) (when-some [s (seq [1 2 3])] s)))
      (is (nil? (when-some [s (seq [])] s))))

    (testing "difference from when-let"
      (is (= false (when-some [x false] x)))
      (is (nil? (when-let [x false] x))))

    (testing "nested when-some"
      (is (= 3 (when-some [x 1]
                 (when-some [y 2]
                   (+ x y)))))
      (is (nil? (when-some [x 1]
                  (when-some [y nil]
                    (+ x y))))))

    (testing "with function call binding"
      (is (= 5 (when-some [x (+ 2 3)] x)))
      (is (= 0 (when-some [x (* 0 100)] x))))

    (testing "with keyword binding"
      (is (= :test (when-some [k :test] k))))

    (testing "side effects in body"
      (let [a (atom 0)]
        (when-some [x 5]
          (swap! a + x))
        (is (= 5 @a)))
      (let [a (atom 0)]
        (when-some [x nil]
          (swap! a inc))
        (is (= 0 @a)))
      (let [a (atom 0)]
        (when-some [x false]
          (swap! a inc))
        (is (= 1 @a))))

    (testing "returns last expression result"
      (is (= :last (when-some [x true]
                     :first
                     :second
                     :last))))

    (testing "with find function"
      (is (= [:a 1] (when-some [e (find {:a 1 :b 2} :a)] e)))
      (is (nil? (when-some [e (find {:a 1} :c)] e))))

    (testing "with get function returning falsey"
      (is (= false (when-some [v (get {:a false} :a)] v)))
      (is (= 0 (when-some [v (get {:a 0} :a)] v)))
      (is (nil? (when-some [v (get {:a 1} :b)] v))))

    (testing "binding shadows outer scope"
      (let [x 10]
        (is (= 20 (when-some [x 20] x)))
        (is (= 10 x))))

    (testing "evaluates binding expression once"
      (let [counter (atom 0)]
        (when-some [x (do (swap! counter inc) true)]
          :body)
        (is (= 1 @counter))))

    (testing "with explicit false value"
      (is (= :done (when-some [b false] :done))))

    (testing "with NaN"
      #?(:clj (is (Double/isNaN (when-some [x ##NaN] x))))
      #?(:cljs (is (js/isNaN (when-some [x ##NaN] x)))))))
