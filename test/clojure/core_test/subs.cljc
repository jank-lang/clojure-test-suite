(ns clojure.core-test.subs
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/subs
  (deftest test-subs
    (is (= "bcde" (subs "abcde" 1)))
    (is (= "bcd" (subs "abcde" 1 4)))
    (is (= "abc" (subs "abcde" 0 3)))
    (is (= "" (subs "abcde" 0 0)))
    (is (= "" (subs "abcde" 5)))
    (is (= "" (subs "abcde" 5 5)))
    #?@(:cljs
        [(is (= "b" (subs "abcde" 2 1)))
         (is (= "bcde" (subs "abcde" 1 6)))
         (is (= "bcde" (subs "abcde" 1 200)))
         (is (= "abcde" (subs "abcde" -1)))
         (is (= "abc" (subs "abcde" -1 3)))
         (is (= "" (subs "abcde" -1 -3)))
         (is (thrown? :default (subs nil 1 2)))
         (is (= "ab" (subs "abcde" nil 2)))
         (is (= "a" (subs "abcde" 1 nil)))]
        :default
        [(is (thrown? #?(:clj Exception) (subs "abcde" 2 1)))
         (is (thrown? #?(:clj Exception) (subs "abcde" 1 6)))
         (is (thrown? #?(:clj Exception) (subs "abcde" 1 200)))
         (is (thrown? #?(:clj Exception) (subs "abcde" -1)))
         (is (thrown? #?(:clj Exception) (subs "abcde" -1 3)))
         (is (thrown? #?(:clj Exception) (subs "abcde" -1 -3)))
         (is (thrown? #?(:clj Exception) (subs nil 1 2)))
         (is (thrown? #?(:clj Exception) (subs "abcde" nil 2)))
         (is (thrown? #?(:clj Exception) (subs "abcde" 1 nil)))])))
