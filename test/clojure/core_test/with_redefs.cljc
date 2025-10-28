(ns clojure.core-test.with-redefs
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(def ^:dynamic *test-var* 10)
(defn test-fn [] 20)

(when-var-exists with-redefs
  (deftest test-basic-redefinition
    (testing "redefines dynamic var temporarily"
      (is (= *test-var* 10))
      (is (= (with-redefs [*test-var* 42]
               *test-var*)
             42))
      (is (= *test-var* 10)))
    
    (testing "redefines function temporarily"
      (is (= (test-fn) 20))
      (is (= (with-redefs [test-fn (fn [] 99)]
               (test-fn))
             99))
      (is (= (test-fn) 20)))
    
    (testing "multiple redefinitions"
      (is (= (with-redefs [*test-var* 1
                           test-fn (fn [] 2)]
               [*test-var* (test-fn)])
             [1 2])))
    
    (testing "nested redefinitions"
      (is (= (with-redefs [*test-var* 1]
               (with-redefs [*test-var* 2]
                 *test-var*))
             2))
      (is (= (with-redefs [*test-var* 1]
               (with-redefs [*test-var* 2]
                 *test-var*)
               *test-var*)
             1))))
  
  (deftest test-nil-handling
    (testing "can redefine to nil"
      (is (= (with-redefs [*test-var* nil]
               *test-var*)
             nil))
      (is (= (with-redefs [test-fn (constantly nil)]
               (test-fn))
             nil)))
    
    (testing "body returns nil"
      (is (nil? (with-redefs [*test-var* 5]
                  nil)))))
  
  (deftest test-body-evaluation
    (testing "evaluates body forms in order"
      (let [result (atom [])]
        (with-redefs [*test-var* 1]
          (swap! result conj *test-var*)
          (swap! result conj *test-var*)
          (swap! result conj *test-var*))
        (is (= @result [1 1 1]))))
    
    (testing "returns last form value"
      (is (= (with-redefs [*test-var* 1]
               1
               2
               3)
             3)))
    
    (testing "empty body returns nil"
      (is (nil? (with-redefs [*test-var* 1])))))
  
  (deftest test-exception-handling
    (testing "restores bindings after exception"
      (is (= *test-var* 10))
      (is (thrown? #?(:clj Exception :cljs js/Error)
                   (with-redefs [*test-var* 99]
                     (throw (ex-info "test" {})))))
      (is (= *test-var* 10)))
    
    (testing "restores function after exception"
      (is (= (test-fn) 20))
      (is (thrown? #?(:clj Exception :cljs js/Error)
                   (with-redefs [test-fn (fn [] 88)]
                     (throw (ex-info "test" {})))))
      (is (= (test-fn) 20))))
  
  (deftest test-visibility
    (testing "redefinition visible in called functions"
      (letfn [(uses-var [] *test-var*)
              (uses-fn [] (test-fn))]
        (is (= (uses-var) 10))
        (is (= (with-redefs [*test-var* 77]
                 (uses-var))
               77))
        (is (= (with-redefs [test-fn (fn [] 88)]
                 (uses-fn))
               88)))))
  
  (deftest test-return-value
    (testing "returns value of body"
      (is (= (with-redefs [*test-var* 1] 42) 42))
      (is (= (with-redefs [*test-var* 1] [1 2 3]) [1 2 3]))
      (is (= (with-redefs [*test-var* 1] {:a 1}) {:a 1})))))
