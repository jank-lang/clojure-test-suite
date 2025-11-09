(ns clojure.core-test.disj-bang
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists disj!
  (deftest test-disj!

    (testing "nominal cases"
      (are [expected set keys] (= expected (persistent! (apply disj! (transient set) keys)))
                               #{} #{} [nil]
                               #{} #{1} [1]
                               #{} #{1} [1 1 1]
                               #{} #{1 2 3} [1 2 3]
                               #{3} #{1 2 3} [1 2]
                               #{1 2 3} #{1 2 3} [4 5 6]
                               #{[3 3]} #{[1 1] 2 [3 3]} [[1 1] 2]
                               #{:a :b} #{:a :b :c} [:c]
                               #{true nil} #{true false nil} [false]))

    (testing "cannot disj! transient after persistent! call"
      (let [t (transient #{1 2 3}), _ (persistent! t)]
        (is (thrown? #?(:cljs js/Error :cljr Exception :default Error) (disj! t 1)))))

    (testing "bad shape"
      (are [set keys] (thrown? #?(:cljs js/Error :default Exception) (apply disj! set keys))
                      nil [nil]
                      #{} [nil]
                      '(1) [1]
                      [1] [1]
                      (transient [1]) [1]
                      {:a 1} [:a]
                      (transient {:a 1}) [:a]
                      42 [42]
                      3.14 [3.14]
                      "string" [\s \t]))))
