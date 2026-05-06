(ns clojure.core-test.uuid-qmark
  (:require [clojure.test :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists uuid?
  (deftest test-uuid?

    (testing "uuids"
      (are [x] (uuid? x)
               #uuid "00000000-0000-0000-0000-000000000000"
               #uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6"
               (parse-uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
               (random-uuid)))

    (testing "not uuids"
      (are [x] (not (uuid? x))
               nil
               true
               false
               0
               1
               -1
               0.0
               1.0
               -1.0
               ##NaN
               ##Inf
               "f81d4fae-7dec-11d0-a765-00a0c91e6bf6"
               {:a :map}
               #{:a-set}
               [:a :vector]
               '(:a :list)
               :a-keyword
               'a-symbol
               (range)
               \a))))
