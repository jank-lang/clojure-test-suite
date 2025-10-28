(ns clojure.core-test.with-bindings
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(def ^:dynamic *test-var* 100)
(def ^:dynamic *another-var* 200)

(when-var-exists with-bindings
  (deftest test-with-bindings
    (testing "binds dynamic var"
      (is (= 42 (with-bindings {#'*test-var* 42}
                  *test-var*))))

    (testing "original value restored"
      (with-bindings {#'*test-var* 999}
        *test-var*)
      (is (= 100 *test-var*)))

    (testing "multiple bindings"
      (is (= 55 (with-bindings {#'*test-var* 10
                                #'*another-var* 45}
                  (+ *test-var* *another-var*)))))

    (testing "nested with-bindings"
      (is (= 500 (with-bindings {#'*test-var* 300}
                   (with-bindings {#'*test-var* 500}
                     *test-var*)))))

    (testing "inner shadows outer"
      (is (= 1 (with-bindings {#'*test-var* 2}
                 (with-bindings {#'*test-var* 1}
                   *test-var*))))
      (is (= 100 *test-var*)))

    (testing "computed binding map"
      (let [bindings {#'*test-var* 777}]
        (is (= 777 (with-bindings bindings *test-var*)))))

    (testing "across function calls"
      (letfn [(get-val [] *test-var*)]
        (is (= 55 (with-bindings {#'*test-var* 55}
                    (get-val))))))

    (testing "returns last expression"
      (is (= :result (with-bindings {#'*test-var* 1} :result))))

    (testing "empty bindings map"
      (is (= 100 (with-bindings {} *test-var*))))

    (testing "with nil binding"
      (is (nil? (with-bindings {#'*test-var* nil} *test-var*))))))
