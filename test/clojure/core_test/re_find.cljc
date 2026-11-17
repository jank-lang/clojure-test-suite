(ns clojure.core-test.re-find
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/re-find

  ;; DOCSTRING:
  ;; clojure.core/re-find
  ;; [m]
  ;; [re s]
  ;; Returns the next regex match, if any, of string to pattern, using
  ;; java.util.regex.Matcher.find().  Uses re-groups to return the
  ;; groups.

  (deftest test-re-find
    (testing "return value shape"
      ;; Without capturing groups, the whole match is returned as a
      ;; plain string.
      (let [result (re-find #"b+" "abbbc")]
        (is (string? result))
        (is (= "bbb" result)))

      ;; With capturing groups, a vector of the whole match followed by
      ;; the groups is returned. The match is in the middle of the
      ;; searched string, so this is not a whole-string match.
      (let [result (re-find #"a(b+)c" "yyabbbczz")]
        (is (vector? result))
        (is (= ["abbbc" "bbb"] result)))

      ;; No match returns `nil`.
      (is (nil? (re-find #"z+" "abbbc")))
      (is (nil? (re-find #"b+" ""))))

    (testing "pattern is searched for, not matched against the whole string"
      (is (= "c" (re-find #"c" "abc")))
      (is (= "aa" (re-find #"a+" "aabaaa")))
      (is (= "1" (re-find #"\d" "a1b2c3")))
      (is (= "dog" (re-find #"cat|dog" "hotdog")))
      ;; The leftmost alternative is preferred, even when a later one
      ;; would consume more input.
      (is (= "a" (re-find #"a|bb" "abb"))))

    (testing "capturing groups"
      (are [expected re s] (= expected (re-find re s))
        ["abbbc" "bbb" "c"]  #"a(b+)(c)"    "xxabbbcyy"
        ["10-20" "10" "20"]  #"(\d+)-(\d+)" "10-20-30"
        ["ab" "ab" "a" "b"]  #"((a)(b))"    "zabz"
        ["bbb" "b"]          #"(b)+"        "abbb"
        ;; A group that only matched the empty string is "", not nil.
        ["b" ""]             #"(a*)b"       "b"))

    ;; A group that did not participate in the match. ClojureCLR reports
    ;; "" where the other dialects report nil.
    (testing "groups that did not participate in the match"
      #?(:cljr (do (is (= ["ac" ""] (re-find #"a(b+)?c" "ac")))
                   (is (= ["dog" "" "dog"] (re-find #"(cat)|(dog)" "hotdog")))
                   (is (= ["a" "a" ""] (re-find #"(a)|(b)" "ab"))))
         :default (do (is (= ["ac" nil] (re-find #"a(b+)?c" "ac")))
                      (is (= ["dog" nil "dog"] (re-find #"(cat)|(dog)" "hotdog")))
                      (is (= ["a" "a" nil] (re-find #"(a)|(b)" "ab"))))))

    (testing "greedy vs. reluctant quantifiers"
      (is (= ["<a><b>" "a><b"] (re-find #"<(.+)>" "<a><b>")))
      (is (= ["<a>" "a"] (re-find #"<(.+?)>" "<a><b>"))))

    (testing "runtime-built patterns"
      (is (= ["abbbc" "bbb"] (re-find (re-pattern "a(b+)c") "xxabbbcyy")))
      (is (= "bbb" (re-find (re-pattern "b+") "abbbc")))
      (is (nil? (re-find (re-pattern "z+") "abbbc"))))

    (testing "anchors, flags and character classes"
      ;; Patterns that can match the empty string match it at position 0.
      (is (= "" (re-find #"" "abc")))
      (is (= "" (re-find #"" "")))
      (is (= "" (re-find #"x*" "abc")))
      (is (= "" (re-find #"b*" "abc")))
      ;; `^` anchors to the whole string unless in multiline mode.
      (is (nil? (re-find #"^b" "a\nb")))
      (is (= "b" (re-find #"(?m)^b" "a\nb\nc")))
      ;; `.` does not match a newline unless in dotall mode.
      (is (nil? (re-find #"a.c" "a\nc")))
      (is (= "a\nc" (re-find #"(?s)a.c" "a\nc")))
      ;; `(?i)` turns on case-insensitive matching for the rest of the
      ;; pattern, so the lowercase pattern matches the uppercase text.
      (is (= "ABC" (re-find #"(?i)abc" "xxABCyy")))
      ;; `\s` is a character class for whitespace, and `\$` escapes the
      ;; `$` anchor so it matches a literal dollar sign.
      (is (= " " (re-find #"\s" "a b")))
      (is (= "$" (re-find #"\$" "cost: $5"))))

    (testing "exception cases"
      (is (p/thrown? (re-find #"a" nil)))
      (is (p/thrown? (re-find #"a" 42)))
      ;; So does a `nil` pattern. ClojureCLR instead returns nil.
      #?(:cljr (is (nil? (re-find nil "abc")))
         :default (is (p/thrown? (re-find nil "abc")))))

    (testing "re-find using re-matcher (arity-1)"
      ;; ClojureScript has no `re-matcher`.
      #?(:cljs nil
         :default
         (do
           ;; Each call returns the next match, and `nil` once there are
           ;; no more matches.
           (let [m (re-matcher #"a(b+)" "xxabbbcyy")]
             (is (= ["abbb" "bbb"] (re-find m)))
             (is (nil? (re-find m))))

           (let [m (re-matcher #"a" "banana")]
             (is (= "a" (re-find m)))
             (is (= "a" (re-find m)))
             (is (= "a" (re-find m)))
             (is (nil? (re-find m))))

           (let [m (re-matcher #"(a)|(b)" "ab")]
             #?(:cljr (do (is (= ["a" "a" ""] (re-find m)))
                          (is (= ["b" "" "b"] (re-find m))))
                :default (do (is (= ["a" "a" nil] (re-find m)))
                             (is (= ["b" nil "b"] (re-find m)))))
             (is (nil? (re-find m))))

           (let [m (re-matcher #"zzz" "abc")]
             (is (nil? (re-find m)))
             ;; A failed match does not make later calls throw.
             (is (nil? (re-find m)))))))))
