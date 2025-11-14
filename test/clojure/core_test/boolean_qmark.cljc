(ns clojure.core-test.boolean-qmark
  (:require clojure.core
            [clojure.core-test.number-range :as r]
            [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists boolean?
  (deftest test-boolean?
    (testing "common"
      (are [expected x] (= expected (boolean? x))
           false nil
           false 0
           false 1
           false -1
           false 0.0
           false 1.0
           false -1.0
           false r/max-int
           false r/min-int
           false \space
           false :a-keyword
           false "str"
           false []
           false '()
           false {}
           false #{}
           true false
           true true
           #?@(:cljs [true (js/Boolean true)
                      true (js/Boolean false)
                      true (js/Boolean "yes")]
               :clj [true (new Boolean "true")
                     true (new Boolean "false")
                     true (new Boolean "yes")])))))
