(ns clojure.core-test.when-let
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists when-let
  (deftest test-when-let
    (testing "binds and executes when truthy"
      (is (= 42 (when-let [x 42] x)))
      (is (= :yes (when-let [x true] :yes))))

    (testing "returns nil when binding is false"
      (is (nil? (when-let [x false] x)))
      (is (nil? (when-let [x false] :never-executed))))

    (testing "returns nil when binding is nil"
      (is (nil? (when-let [x nil] x)))
      (is (nil? (when-let [x nil] :never-executed))))

    (testing "binding available in body"
      (is (= 10 (when-let [x 5] (* x 2))))
      (is (= "hello" (when-let [s "hello"] s))))

    (testing "multiple expressions in body"
      (is (= 6 (when-let [x 2]
                 (* x 2)
                 (* x 3)))))

    (testing "with collection bindings"
      (is (= [1 2 3] (when-let [v [1 2 3]] v)))
      (is (= {:a 1} (when-let [m {:a 1}] m)))
      (is (= #{1 2} (when-let [s #{1 2}] s))))

    (testing "with empty collection (truthy)"
      (is (= [] (when-let [v []] v)))
      (is (= {} (when-let [m {}] m)))
      (is (= #{} (when-let [s #{}] s))))

    (testing "with seq function"
      (is (= '(1 2 3) (when-let [s (seq [1 2 3])] s)))
      (is (nil? (when-let [s (seq [])] s))))

    (testing "with first function"
      (is (= 1 (when-let [x (first [1 2 3])] x)))
      (is (nil? (when-let [x (first [])] x))))

    (testing "nested when-let"
      (is (= 3 (when-let [x 1]
                 (when-let [y 2]
                   (+ x y)))))
      (is (nil? (when-let [x 1]
                  (when-let [y nil]
                    (+ x y))))))

    (testing "with function call binding"
      (is (= 5 (when-let [x (+ 2 3)] x)))
      (is (= [2 3 4] (when-let [v (map inc [1 2 3])] (vec v)))))

    (testing "with string binding"
      (is (= "test" (when-let [s "test"] s)))
      (is (= "" (when-let [s ""] s))))

    (testing "with number binding"
      (is (= 42 (when-let [n 42] n)))
      (is (= 0 (when-let [n 0] n))))

    (testing "with keyword binding"
      (is (= :test (when-let [k :test] k))))

    (testing "destructuring not supported in when-let"
      (is (= [1 2] (when-let [v [1 2]] v))))

    (testing "side effects in body"
      (let [a (atom 0)]
        (when-let [x 5]
          (swap! a + x))
        (is (= 5 @a)))
      (let [a (atom 0)]
        (when-let [x nil]
          (swap! a inc))
        (is (= 0 @a))))

    (testing "returns last expression result"
      (is (= :last (when-let [x true]
                     :first
                     :second
                     :last))))

    (testing "with boolean true"
      (is (= true (when-let [b true] b))))

    (testing "binding shadows outer scope"
      (let [x 10]
        (is (= 20 (when-let [x 20] x)))
        (is (= 10 x))))

    (testing "with find function"
      (is (= [:a 1] (when-let [e (find {:a 1 :b 2} :a)] e)))
      (is (nil? (when-let [e (find {:a 1} :c)] e))))

    (testing "with get function"
      (is (= 1 (when-let [v (get {:a 1} :a)] v)))
      (is (nil? (when-let [v (get {:a 1} :b)] v))))

    (testing "with re-find"
      (is (= "test" (when-let [m (re-find #"test" "this is a test")] m)))
      (is (nil? (when-let [m (re-find #"xyz" "test")] m))))

    (testing "evaluates binding expression once"
      (let [counter (atom 0)]
        (when-let [x (do (swap! counter inc) true)]
          :body)
        (is (= 1 @counter))))))
