(ns clojure.core-test.symbol-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists symbol?
 (deftest test-symbol?
   (are [expected x] (= expected (symbol? x))
     true  'a-symbol
     true  'a-ns/a-keyword

     false :a-keyword
     false :a-ns/a-keyword
     false "a string"
     false 0
     false 0N
     false 0.0
     false 0.0M
     false false
     false true
     false nil
     #?@(:cljs []
         :default
         [false 1/2]))))
