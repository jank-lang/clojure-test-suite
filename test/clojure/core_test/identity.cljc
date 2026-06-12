(ns clojure.core-test.identity
  (:require [clojure.test :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

; Values for tests are declared eagerly, instead of using
; clojure.test/are. Some dialects evaluate `are` params more
; than once, causing false negatives with object equality.
(def test-vals
  [nil
   true
   false
   ""
   "foo"
   \a
   :foo
   :foo.bar/baz
   'foo
   'foo.bar/baz
   0
   1
   0.1
   -0.1
   ##Inf
   ##-Inf
   []
   (list)
   #uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6"
   #inst "2010-11-12T13:14:15.666-05:00"
   (atom nil)])

(when-var-exists identity
  (deftest test-identity
    (is (NaN? (identity ##NaN))) 
    (doseq [value test-vals]
      (is (identical? value (identity value))))))
