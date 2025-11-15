(ns clojure.core-test.not
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists not
  (deftest test-not
    (testing "common"
      (are [given expected] (= expected (not given))
        nil                    true
        false                  true
        true                   false
        #?(:clj (Object.)
           :cljr (Object.)
           :cljs #js {}
           :default :anything) false))

    (testing "infinite-sequence"
      (is (= false (not (range)))))))
