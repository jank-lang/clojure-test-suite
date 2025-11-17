(ns clojure.core-test.name
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists name
 (deftest test-name
   (are [expected x] (= expected (name x))
     "abc" "abc"
     "abc" :abc
     "abc" 'abc
     "def" :abc/def
     "def" 'abc/def
     "abc*+!-_'?<>=" :abc/abc*+!-_'?<>=
     "abc*+!-_'?<>=" 'abc/abc*+!-_'?<>=)

   (is (thrown? #?(:cljs :default :default Exception) (name nil)))))
