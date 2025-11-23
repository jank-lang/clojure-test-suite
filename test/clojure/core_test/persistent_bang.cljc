(ns clojure.core-test.persistent-bang
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists persistent!
  (deftest test-persistent!

    (testing "map"
      (are [expected coll] (= expected (persistent! coll))
                           {} (transient {})
                           {nil nil} (transient {nil nil})
                           {:a 1 :b 2} (transient {:a 1 :b 2})))

    (testing "vector"
      (are [expected coll] (= expected (persistent! coll))
                           [] (transient [])
                           [nil] (transient [nil])
                           [1 2 3] (transient [1 2 3])))

    (testing "set"
      (are [expected coll] (= expected (persistent! coll))
                           #{} (transient #{})
                           #{nil} (transient #{nil})
                           #{:a :b :c} (transient #{:a :b :c})))

    (testing "calling persistent! a second time throws"
      (let [coll (transient {}), _ (persistent! coll)]
        (is (thrown? #?(:cljs js/Error :cljr Exception :default Error) (persistent! coll)))))

    (testing "bad shape"
      (are [coll] (thrown? #?(:cljs js/Error :default Exception) (persistent! coll))
                  nil
                  {:a 1 :b 2}
                  [1 2 3]
                  '(1 2 3)
                  #{1 2 3}
                  true
                  false))))
