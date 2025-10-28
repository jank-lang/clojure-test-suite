(ns clojure.core-test.var-get
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(def ^:dynamic *test-var* 42)

(when-var-exists var-get
  (deftest test-var-get
    (testing "get value from var"
      (is (= 42 (var-get #'*test-var*)))
      (is (= 42 (var-get (var *test-var*)))))

    (testing "get value from built-in vars"
      (is (= clojure.core/+ (var-get #'+)))
      (is (fn? (var-get #'map))))

    (testing "get value from dynamic var"
      (binding [*test-var* 100]
        (is (= 100 (var-get #'*test-var*)))))

    (testing "get value after def"
      (def test-local-var 123)
      (is (= 123 (var-get #'test-local-var))))

    (testing "get nil value"
      (def test-nil-var nil)
      (is (nil? (var-get #'test-nil-var))))

    (testing "get various types"
      (def test-string-var "hello")
      (def test-vector-var [1 2 3])
      (def test-map-var {:a 1})
      (def test-keyword-var :test)
      (is (= "hello" (var-get #'test-string-var)))
      (is (= [1 2 3] (var-get #'test-vector-var)))
      (is (= {:a 1} (var-get #'test-map-var)))
      (is (= :test (var-get #'test-keyword-var))))

    (testing "get function value"
      (defn test-fn [x] (* x 2))
      (is (fn? (var-get #'test-fn)))
      (is (= 10 ((var-get #'test-fn) 5))))

    (testing "with thread-local bindings"
      (binding [*test-var* 999]
        (is (= 999 (var-get #'*test-var*)))
        (binding [*test-var* 1000]
          (is (= 1000 (var-get #'*test-var*)))))
      (is (= 42 (var-get #'*test-var*))))

    (testing "get value of altered var"
      (def test-mutable-var 1)
      (alter-var-root #'test-mutable-var inc)
      (is (= 2 (var-get #'test-mutable-var))))

    (testing "equality with deref"
      (is (= (var-get #'*test-var*) @#'*test-var*))
      (is (= (var-get #'+) @#'+)))))
