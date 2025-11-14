(ns clojure.core-test.char
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists char
 (deftest test-char
   (are [expected x] (= expected (char x))
     ;; Assumes ASCII / Unicode
     \space 32
     \@     64
     \A     65
     \A     \A
     ;; TODO: Add Unicode tests
     )

   #?(:cljs nil :default (is (thrown? Exception (char -1))))
   (is (thrown? #?(:cljs :default :default Exception) (char nil)))))
