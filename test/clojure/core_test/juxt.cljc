(ns clojure.core-test.juxt
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists juxt
  (deftest test-juxt
    (testing "'returns a function'"
      (is (ifn? (juxt juxt partial comp :foo)))
      (is (ifn? (juxt nil)))
      (is (ifn? (juxt (range)))))

    (testing "'the returned fn...returns a vector'"
      (is (vector? ((juxt inc) 5))))

    (testing "'The returned fn takes a variable number of args'"
      (is (= ["12345678910" 1 10]
             ((juxt str min max) 1 2 3 4 5 6 7 8 9 10))))

    (testing "'returns a fn that is the juxtaposition of those fns'"
      (is (let [arg 10]
            (= (vector (inc arg) (dec arg) (identity arg))
               ((juxt inc dec identity) arg))))
      (is (= ["foo" nil "bar"]
             ((juxt :foo :missing :bar) {:foo "foo", :bar "bar"})))
      (is (= [:namespace/keyword "keyword" "namespace"]
             ((juxt identity name namespace) :namespace/keyword)))
      (is (= [:v] ((juxt {:k :v}) :k)))
      (is (= [:v] ((juxt :k)      {:k :v})))

      (is (= [:v :value "value"]
             ((juxt {:k :v}
                    #(get {:k :value} %)
                    (fn [x] (get {:k "value"} x)))
              :k))))

    (testing "wrong-shape input is mostly accepted (and throws when invoked)"
      #?(:cljs (is (thrown? :default ((juxt nil))))
         :clj  (is (thrown? java.lang.NullPointerException ((juxt nil))))
         :clr  (is (thrown? System.NullReferenceException ((juxt nil)))))
      #?@(:cljs    [(is (thrown? :default ((juxt (range)))))
                    (is (thrown? :default ((juxt 1))))]
          :default [(is (thrown? Exception ((juxt (range)))))
                    (is (thrown? Exception ((juxt 1))))]))))
