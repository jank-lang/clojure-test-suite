(ns clojure.core-test.with-redefs-fn
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(def test-var 10)
(defn test-fn [] 20)
(def ^:dynamic *dynamic-var* 30)

(when-var-exists with-redefs-fn
  (deftest test-basic-redefinition
    (testing "temporarily redefines var"
      (is (= 99 
             (with-redefs-fn {#'test-var 99}
               (fn [] test-var))))
      (is (= 10 test-var)))

    (testing "redefines function"
      (is (= :mocked 
             (with-redefs-fn {#'test-fn (fn [] :mocked)}
               (fn [] (test-fn)))))
      (is (= 20 (test-fn))))
    
    (testing "redefines dynamic var"
      (is (= 77
             (with-redefs-fn {#'*dynamic-var* 77}
               (fn [] *dynamic-var*))))
      (is (= 30 *dynamic-var*))))

  (deftest test-multiple-vars
    (testing "multiple vars at once"
      (is (= [1 2 3]
             (with-redefs-fn {#'test-var 1
                              #'test-fn (fn [] 2)
                              #'*dynamic-var* 3}
               (fn [] [test-var (test-fn) *dynamic-var*])))))
    
    (testing "original values restored"
      (with-redefs-fn {#'test-var 99 #'test-fn (fn [] 88)}
        (fn [] nil))
      (is (= 10 test-var))
      (is (= 20 (test-fn)))))

  (deftest test-nil-handling
    (testing "can redefine to nil"
      (is (nil? (with-redefs-fn {#'test-var nil}
                  (fn [] test-var)))))
    
    (testing "function returns nil"
      (is (nil? (with-redefs-fn {#'test-var 1}
                  (fn [] nil))))))

  (deftest test-nested
    (testing "nested with-redefs-fn"
      (is (= :inner
             (with-redefs-fn {#'test-var :outer}
               (fn []
                 (with-redefs-fn {#'test-var :inner}
                   (fn [] test-var))))))
      (is (= 10 test-var)))
    
    (testing "outer binding visible after inner"
      (is (= [:inner :outer]
             (with-redefs-fn {#'test-var :outer}
               (fn []
                 (let [inner-result (with-redefs-fn {#'test-var :inner}
                                      (fn [] test-var))]
                   [inner-result test-var])))))))

  (deftest test-exception-handling
    (testing "restores bindings after exception"
      (is (= 10 test-var))
      (is (thrown? #?(:clj Exception :cljs js/Error)
                   (with-redefs-fn {#'test-var 99}
                     (fn [] (throw (ex-info "test" {}))))))
      (is (= 10 test-var))))

  (deftest test-empty-bindings
    (testing "empty bindings map"
      (is (= 42 (with-redefs-fn {} (fn [] 42)))))
    
    (testing "original values unchanged"
      (with-redefs-fn {} (fn [] nil))
      (is (= 10 test-var))
      (is (= 20 (test-fn)))))

  (deftest test-function-result
    (testing "returns function result"
      (is (= 42 (with-redefs-fn {#'test-var 1} (fn [] 42))))
      (is (= [1 2 3] (with-redefs-fn {#'test-var 1} (fn [] [1 2 3]))))
      (is (= {:a 1} (with-redefs-fn {#'test-var 1} (fn [] {:a 1})))))))
