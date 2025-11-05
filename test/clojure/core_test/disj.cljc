(ns clojure.core-test.disj
  (:require [clojure.test :refer [deftest testing are is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists disj
  (deftest test-disj
    (testing "nominal cases"
      (are [expected set keys] (= expected (apply disj set keys))
                               nil nil [nil]
                               #{} #{} [nil]
                               #{} #{1} [1]
                               #{} #{1} [1 1 1]
                               #{} #{1 2 3} [1 2 3]
                               #{3} #{1 2 3} [1 2]
                               #{1 2 3} #{1 2 3} [4 5 6]
                               #{[3 3]} #{[1 1] 2 [3 3]} [[1 1] 2]
                               #{:a :b} #{:a :b :c} [:c]
                               #{true nil} #{true false nil} [false]))
    (testing "sorted preservation"
      (is (sorted? (disj (sorted-set 1 2 3) 1 2 3))))
    (testing "meta preservation"
      (let [test-meta {:me "ta"}
            with-test-meta #(with-meta % test-meta)
            with-test-meta? #(= test-meta (meta %))]
        (is (with-test-meta? (disj (with-test-meta #{1 2 3}) 1 2 3)))))
    (testing "bad shape"
      (are [set keys] (thrown? #?(:cljs js/Error :default Exception) (apply disj set keys))
                      '(1) [1]
                      [1] [1]
                      {:a 1} [:a]
                      42 [42]
                      3.14 [3.14]
                      "string" [\s \t]))))
