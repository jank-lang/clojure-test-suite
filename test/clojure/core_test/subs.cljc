(ns clojure.core-test.subs
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists subs
  (deftest test-subs
    (testing "ASCII only characters"
      (is (= "abcde" (subs "abcde" 0)))
      (is (= "abcde" (subs "abcde" 0 5)))
      (is (= "bcde" (subs "abcde" 1)))
      (is (= "bcd" (subs "abcde" 1 4)))
      (is (= "abc" (subs "abcde" 0 3)))
      (is (= "" (subs "" 0 0)))
      (is (= "" (subs "" 0)))
      (is (= "" (subs "abcde" 0 0)))
      (is (= "" (subs "abcde" 5)))
      (is (= "" (subs "abcde" 5 5)))
      (is (= "" (subs "abcde" 4 4))))

    (testing "Unicode characters"
      (case (p/string-encoding)
        :utf16
        (testing "UTF-16"
          (is (= "ab֎de" (subs "ab֎de" 0)))
          (is (= "ab֎de" (subs "ab֎de" 0 5)))
          (is (= "bcde" (subs "֎bcde" 1)))
          (is (= "bcd" (subs "֎bcde" 1 4)))
          (is (= "ab֎" (subs "ab֎de" 0 3)))
          (is (= "" (subs "֎bcde" 0 0)))
          (is (= "" (subs "abcd֎" 5)))
          (is (= "" (subs "abcd֎" 5 5)))
          (is (= "" (subs "abc֎e" 4 4))))
        :utf8
        (testing "UTF-8"
          (is (= "ab֎de" (subs "ab֎de" 0)))
          (is (= "ab֎de" (subs "ab֎de" 0 6)))
          (is (= "bcde" (subs "֎bcde" 2)))
          (is (= "bcd" (subs "֎bcde" 2 5)))
          (is (= "ab֎" (subs "ab֎de" 0 4)))
          (is (= "" (subs "֎bcde" 0 0)))
          (is (= "" (subs "abcd֎" 6)))
          (is (= "" (subs "abcd֎" 6 6)))
          (is (= "" (subs "abc֎e" 4 4))))))

    #?@(:lpy
        ;; Directly delegate to Python's slicing syntax.
        ;; s[2:1] just returns an empty string, rather than throwing an exception.
        [(is (= "" (subs "abcde" 2 1)))
         (is (= "bcde" (subs "abcde" 1 6)))
         (is (= "bcde" (subs "abcde" 1 200)))
         (is (= "e" (subs "abcde" -1)))
         (is (= "" (subs "abcde" -1 3)))
         (is (= "" (subs "abcde" -1 -3)))
         (is (p/thrown? (subs nil 1 2)))
         (is (= "ab" (subs "abcde" nil 2)))
         (is (= "bcde" (subs "abcde" 1 nil)))]

        :cljs
        [(is (= "b" (subs "abcde" 2 1)))
         (is (= "bcde" (subs "abcde" 1 6)))
         (is (= "bcde" (subs "abcde" 1 200)))
         (is (= "abcde" (subs "abcde" -1)))
         (is (= "abc" (subs "abcde" -1 3)))
         (is (= "" (subs "abcde" -1 -3)))
         (is (= "a" (subs \a 0)))
         (is (p/thrown? (subs nil 1 2)))
         (is (= "ab" (subs "abcde" nil 2)))
         (is (= "a" (subs "abcde" 1 nil)))]
        
        :default
        [(is (p/thrown? (subs "abcde" 2 1)))
         (is (p/thrown? (subs "abcde" 1 6)))
         (is (p/thrown? (subs "abcde" 1 200)))
         (is (p/thrown? (subs "abcde" -1)))
         (is (p/thrown? (subs "abcde" -1 3)))
         (is (p/thrown? (subs "abcde" -1 -3)))
         (is (p/thrown? (subs nil 1 2)))
         (is (p/thrown? (subs "abcde" nil 2)))
         (is (p/thrown? (subs "abcde" 1 nil)))])))
