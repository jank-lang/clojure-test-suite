(ns clojure.core-test.conj-bang
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists conj!
  (deftest test-conj!

    (testing "conj!"
      (is (= [] (persistent! (conj!)))))

    (testing "conj! coll"
      (testing "when coll is transient"
        (are [expected coll] (= expected (persistent! (conj! (transient coll))))
                             [] []
                             [1 2 3] [1 2 3]
                             {} {}
                             {:a 1 :b 2 :c 3} {:a 1 :b 2 :c 3}
                             #{} #{}
                             #{1} #{1}))
      (testing "when coll is anything else"
        (are [coll] (= coll (conj! coll))
                    nil
                    []
                    [1 2 3]
                    {}
                    {:a 1 :b 2 :c 3}
                    '()
                    '(:a :b :c)
                    (range 1 4)
                    #{}
                    #{1 2 3}
                    "abc"
                    0
                    0.0
                    :k
                    true
                    false)))

    (testing "conj! coll x"
      (testing "when x is nil or empty"
        (are [expected coll x] (= expected (persistent! (conj! coll x)))
                               [nil] (transient []) nil
                               [[]] (transient []) []
                               {} (transient {}) nil
                               {} (transient {}) {}
                               #{nil} (transient #{}) nil
                               #{#{}} (transient #{}) #{}))
      (testing "when x is non-nil and non-empty"
        (are [expected coll x] (= expected (persistent! (conj! coll x)))
                               [1] (transient []) 1
                               [1 [2]] (transient [1]) [2]
                               [1 2 3 4] (conj! (transient [1 2]) 3) 4
                               {:a 1} (transient {}) {:a 1}
                               {:a 1 :b 2} (transient {:a 1}) [:b 2]
                               {:a 2 :b 1} (transient {:a 1 :b 2}) {:a 2 :b 1}
                               {:a 1 :b 2 :c 3} (conj! (transient {:a 0}) {:b 2}) {:a 1 :c 3}
                               #{1} (transient #{}) 1
                               #{1 #{2}} (transient #{1}) #{2}
                               #{1 2 3 4} (conj! (transient #{1 2}) 3) 4)))

    (testing "cannot conj! after call to persistent!"
      (let [coll (transient []), _ (persistent! coll)]
        (is (thrown? #?(:cljs js/Error :cljr Exception :default Error) (conj! coll 0)))))

    (testing "bad shapes"
      (are [coll x] (thrown? #?(:cljs js/Error :default Exception) (conj! coll x))
                    (transient {}) '(:a 1)
                    (transient {}) #{:a 1}
                    (transient {}) (range 2)
                    [] 1
                    {} {:a 1}
                    '() true
                    #{} :k
                    "abc" \d
                    true false
                    1 -1
                    (range 3) -1))))
