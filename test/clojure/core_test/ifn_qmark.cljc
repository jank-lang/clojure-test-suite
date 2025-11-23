(ns clojure.core-test.ifn-qmark
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(defn foo [x] (str "hello " x))

(when-var-exists defprotocol
  (defprotocol Bar (bar [this a b] "bar docstring")))

(when-var-exists ifn?
  (deftest test-ifn?
    (testing "`ifn?`"
      (testing "functions, functions from HOFs, transducers, #() reader macro, `fn`, `defn`"
        (is (ifn? juxt))
        (is (ifn? (juxt inc dec)))
        (is (ifn? (map inc)))
        (is (ifn? #(str "hello " %)))
        (is (ifn? (fn [x] (str "hello " x))))
        (is (ifn? foo)))

      (testing "function-like things"
        (is (ifn? {:a :b}))
        (is (ifn? #{:a :b :c}))
        (is (ifn? [:a :b]))
        (is (ifn? :keyword))
        (is (ifn? 'symbol))
        (is (ifn? #'ifn?))
        (when-var-exists promise
          (is (ifn? (promise)))))

      (testing "non-functions"
        ;; atomic values are not function-like
        (is (not (ifn? nil)))
        (is (not (ifn? 12345678)))
        (is (not (ifn? "string")))
        (is (not (ifn? \space)))))

    (when-var-exists defmulti
      (testing "multimethods"
        (defmulti my-multi first)
        (defmethod my-multi :foo [_] :multi/foo)
        (is (ifn? my-multi))))

    (when-var-exists defprotocol
      (testing "protocols"
        (is (ifn? bar))))))
