(ns clojure.string-test.join
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.string/join

  ;; DOCSTRING:
  ;; clojure.string/join
  ;; [coll]
  ;; [separator coll]
  ;; Returns a string of all elements in coll, as returned by (seq coll),
  ;; separated by an optional separator.

  (deftest test-join
    (testing "one-arity - no separator"
      (testing "empty colls"
        (is (= "" (str/join [])))
        (is (= "" (str/join ()))))

      (testing "returns a string"
        (is (string? (str/join [])))
        (is (string? (str/join [1 2 3]))))

      (testing "elements are stringified and concatenated"
        (are [expected coll] (= expected (str/join coll))
          "1"          [1]
          "123"        [1 2 3]
          "abc"        [\a \b \c]
          "abc"        "abc"
          "abc"        (list "a" "b" "c")
          "123"        (range 1 4)
          "123"        (map inc (range 3))
          ":abc"       [:a 'b "c"]
          "[1 2][3 4]" [[1 2] [3 4]]
          "1"          #{1}))

      (testing "nil elements are treated as empty strings"
        (are [expected coll] (= expected (str/join coll))
          ""   [nil]
          ""   [nil nil]
          ""   [nil nil nil]
          "1"  [1 nil]
          "1"  [nil 1]
          "12" [1 nil 2]))

      (testing "nil coll"
        (is (= "" (str/join nil)))))

    (testing "two-arity - with separator"
      (testing "empty colls"
        (are [expected separator] (= expected (str/join separator []))
          "" ","
          "" "--"
          "" ""
          "" nil))

      (testing "separator is placed only between elements"
        (are [expected separator coll] (= expected (str/join separator coll))
          "1"     ","   [1]
          "1,2,3" ","   [1 2 3]
          "123"   ""    [1 2 3]
          "a-b-c" "-"   "abc"
          "a-b-c" "-"   [\a \b \c]
          "a--b"  "--"  ["a" "b"]
          "a|||b" "|||" ["a" "b"]
          "a▒b"   "▒"   ["a" "b"]
          "a b"   " "   ["a" "b"]))

      (testing "nil separator acts like no separator"
        #?(:cljs
           ;; CLJS stringifies a non-string separator, so nil becomes "null"
           (are [expected coll] (= expected (str/join nil coll))
             ""           []
             "1"          [1]
             "1null2"     [1 2]
             "1nullnull2" [1 nil 2])

           :default
           (are [coll] (= (str/join coll) (str/join nil coll))
             []
             [1]
             [1 2 3]
             [1 nil 3]
             "abc"
             (range 5))))

      (testing "nil colls"
        (are [separator] (= "" (str/join separator nil))
          ","
          ""
          nil))

      (testing "nil elements are treated as empty strings"
        (are [expected separator coll] (= expected (str/join separator coll))
          ""     ","  [nil]
          ""     ""   [nil nil]
          "1,,2" ","  [1 nil 2]
          "1,2," ","  [1 2 nil]
          ",1,2" ","  [nil 1 2]))

      (testing "non-string elements are stringified"
        (are [expected separator coll] (= expected (str/join separator coll))
          "1,2,3"       "," (range 1 4)
          "[1 2],[3 4]" "," [[1 2] [3 4]]
          ":a,b,c"      "," [:a 'b "c"]
          "1"           "," #{1})))

    (testing "non-seqable colls"
      (is (p/thrown? (str/join 1)))
      (is (p/thrown? (str/join "," 1))))))
