(ns clojure.core-test.some-fn
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists some-fn
  (deftest test-some-fn

    (testing "one predicate"
      (is (= false ((some-fn even?) 1)))
      (is (= true ((some-fn even?) 2)))
      (is (= true ((some-fn even?) 1 2))))

    (testing "multiple predicates"
      (is (= false ((some-fn neg? #(> % 10)) 1 2 3)))
      (is (= true ((some-fn neg? #(> % 10) #(= % 3)) 1 2 3)))
      (is (= true ((some-fn neg? #(> % 10) #(= % 3) #(= % 5)) 1 2 5))))

    (testing "supports ifn"
      (is (= 3 ((some-fn #{1 2} #{3 4}) 5 3 7))))

    (testing "short-circuits"
      (is (= true ((some-fn even? (fn [_] (assert false "pred should have been short-circuited"))) 2)))
      (is (= true ((some-fn even?) 2 "arg should have been short-circuited"))))

    (testing "bad shape"
      #?(:cljs    (is (= nil ((#'some-fn))))
         :default (is (thrown? Exception (some-fn))))
      (are [pred] (= true (fn? (some-fn pred)))
                  "not a fn"
                  42
                  3.14)
      (are [pred] (thrown? #?(:cljs js/Error :default Exception) ((some-fn pred) nil))
                  "not a fn"
                  42
                  3.14))))
