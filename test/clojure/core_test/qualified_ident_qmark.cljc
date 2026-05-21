(ns clojure.core-test.qualified-ident-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists qualified-ident?
 (deftest test-qualified-ident?
   (are [expected x] (= expected (qualified-ident? x))
     ;; CLJS will fail to read 'a/b/c and :a/b/c even with reader conditionals,
     ;; there is similar behavior with dialect specific regex literals
     #?(:cljs false :default true) (keyword "a/b/c")
     true  (symbol "a/b/c")
     true  ::a-keyword
     true  :a-ns/a-keyword
     true  'a-ns/a-keyword

     false :a-keyword
     false 'a-symbol
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
