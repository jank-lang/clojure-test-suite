(ns clojure.core-test.volatile-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists volatile?
  (deftest test-volatile-qmark
    (testing "returns true for volatile"
      (is (volatile? (volatile! 42)))
      (is (volatile? (volatile! nil)))
      (is (volatile? (volatile! "test"))))

    (testing "returns false for non-volatiles"
      (is (not (volatile? 42)))
      (is (not (volatile? nil)))
      (is (not (volatile? "test")))
      (is (not (volatile? [])))
      (is (not (volatile? {})))
      (is (not (volatile? #{})))
      (is (not (volatile? '()))))

    (testing "returns false for atom"
      (is (not (volatile? (atom 42)))))

    (testing "returns false for ref"
      #?(:clj (is (not (volatile? (ref 42))))))

    (testing "returns false for agent"
      #?(:clj (is (not (volatile? (agent 42))))))

    (testing "returns false for var"
      (is (not (volatile? #'map))))

    (testing "returns false for keywords"
      (is (not (volatile? :test)))
      (is (not (volatile? :namespaced/key))))

    (testing "returns false for symbols"
      (is (not (volatile? 'foo)))
      (is (not (volatile? 'namespaced/sym))))

    (testing "returns false for strings"
      (is (not (volatile? "")))
      (is (not (volatile? "hello"))))

    (testing "returns false for numbers"
      (is (not (volatile? 0)))
      (is (not (volatile? 42)))
      (is (not (volatile? -1)))
      (is (not (volatile? 3.14)))
      #?(:clj (is (not (volatile? 1/2)))))

    (testing "returns false for booleans"
      (is (not (volatile? true)))
      (is (not (volatile? false))))

    (testing "returns false for functions"
      (is (not (volatile? +)))
      (is (not (volatile? (fn [] 42)))))

    (testing "returns false for characters"
      #?(:clj (is (not (volatile? \a)))))

    (testing "returns true after vreset!"
      (let [v (volatile! 1)]
        (vreset! v 2)
        (is (volatile? v))))

    (testing "returns true after vswap!"
      (let [v (volatile! 1)]
        (vswap! v inc)
        (is (volatile? v))))

    (testing "volatile check on deref'd value"
      (let [v (volatile! 42)]
        (is (not (volatile? @v)))))

    (testing "multiple volatiles"
      (let [v1 (volatile! 1)
            v2 (volatile! 2)
            v3 (volatile! 3)]
        (is (volatile? v1))
        (is (volatile? v2))
        (is (volatile? v3))))

    (testing "volatile with different initial values"
      (are [val] (volatile? (volatile! val))
        nil
        0
        42
        "string"
        []
        {}
        #{}
        :keyword
        'symbol
        +))))
