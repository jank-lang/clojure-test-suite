(ns clojure.core-test.qualified-symbol-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/qualified-symbol?
 (deftest test-qualified-symbol?
   (are [expected x] (= expected (qualified-symbol? x))
     false :a-keyword
     false 'a-symbol
     false :a-ns/a-keyword
     true  'a-ns/a-keyword
     false "a string"
     false 0
     false 0N
     false 0.0
     #?@(:cljs []
         :default
         [false 1/2])
     false 0.0M
     false false
     false true
     false nil)))
