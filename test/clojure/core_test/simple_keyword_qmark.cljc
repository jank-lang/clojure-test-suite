(ns clojure.core-test.simple-keyword-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists simple-keyword?
 (deftest test-simple-keyword?
   (are [expected x] (= expected (simple-keyword? x))
     true  :a-keyword

     false 'a-symbol
     false :a-ns/a-keyword
     false 'a-ns/a-keyword
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
