(ns clojure.core-test.remove
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists remove
  (deftest test-remove
    (testing "Single argument returns a transducer"
      (is (= [1] (transduce (comp (remove nil?) (remove even?))
                            conj
                            [nil 0 nil 1 nil 2]))))

    (testing "Two arguments"
      (testing "pred returning nil keeps the item"
        (is (= '(nil false) (remove identity [nil true false 0 ""]))))
      (testing "empty and nil collections"
        (is (= '() (remove identity nil)))
        (is (= '() (remove identity []))))
      (testing "infinite collection"
        (let [res (remove (partial < 5) (range))]
          (is (p/lazy-seq? res))
          (is (not (realized? res)))
          (is (= '(0 1 2) (take 3 res)))))
      (is (= #{:b :c :d :e} (into #{} (remove #{:a} #{:a :b :c :d :e})))))

    (testing "Exception cases"
      (testing "non function passed as first argument throws"
        (are [x] (p/thrown? (first (remove x [0])))
          \a
          ""
          #""
          0
          nil))
      (testing "non collection passed as second argument throws"
        (are [x] (p/thrown? (first (remove nil? x)))
          #""
          0
          (fn [])
          (atom nil))
        #?(:cljs    (is (= \a (first (remove nil? \a))))
           :lpy     (is (= \a (first (remove nil? \a))))
           :default (is (p/thrown? (first (remove nil? \a)))))))))
