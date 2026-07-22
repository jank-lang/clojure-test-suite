(ns clojure.core-test.name
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists name

  ;; ([x])
  ;; Returns the name String of a string, symbol or keyword.

  (deftest test-name
    (are [expected x] (= expected (name x))
      ;; empty string. note that we can generate keywords and symbols
      ;; with empty names via `(keyword "")` and `(symbol "")`, and
      ;; pass the output to `name` and get back the empty string, but
      ;; the reader cannot read such keywords or symbols.
      "" ""

      ;; the simple cases
      "abc" "abc"
      "abc" :abc
      "abc" 'abc

      ;; namespace keywords and symbols just return the name, not the
      ;; namespace.
      "def" :abc/def
      "def" 'abc/def

      ;; try some names with a range of special characters
      "abc*+!-_'?<>=" :abc/abc*+!-_'?<>=
      "abc*+!-_'?<>=" 'abc/abc*+!-_'?<>=)

    (is (p/thrown? (name nil)))))
