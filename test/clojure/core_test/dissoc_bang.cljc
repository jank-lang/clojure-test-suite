(ns clojure.core-test.dissoc-bang
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists dissoc!

  (deftest test-dissoc!

    (testing "non-nil"
      (are [expected m keys] (= expected (persistent! (apply dissoc! (transient m) keys)))
                             {} {} [:a]
                             {} {:a 1} [:a]
                             {} {:a 1} [:a :a]
                             {} {:a 1 :b 2} [:a :b]
                             {:b 2} {:a 1 :b 2} [:a]
                             {:b 2} {:a 1 :b 2} [:a :c]))

    (testing "nil"
      (are [expected m keys] (= expected (persistent! (apply dissoc! (transient m) keys)))
                             {} {} [nil]
                             {} {nil nil} [nil]
                             {} {nil nil} [nil nil]))

    (testing "bad shape"
      (are [m keys] (thrown? #?(:cljs js/Error :default Exception) (apply dissoc! m keys))
                    {:a 1} [:a]
                    [0] [0]
                    (transient [0]) [0]
                    '(0) [0]
                    #{:a :b} [:a]
                    (transient #{:a :b}) [:a]
                    42 [4]
                    :k [:k]
                    "string" [\s \t]))))
