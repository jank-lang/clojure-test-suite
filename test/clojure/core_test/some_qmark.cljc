(ns clojure.core-test.some-qmark
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists some?
  (deftest test-some?
    (testing "common"
      (are [given expected] (= expected (some? given))
        nil false
        true true
        false true
        #?(:clj (Object.)
           :cljs #js {}
           :default :anything) true))

    (testing "infinite-sequence"
      (is (= true (some? (range)))))))
