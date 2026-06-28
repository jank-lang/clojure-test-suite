(ns clojure.string-test.replace
  "Replaces all instance of match with replacement in s.

   match/replacement can be:

   string / string
   char / char
   pattern / (string or function of match).

   See also replace-first.

   The replacement is literal (i.e. none of its characters are treated
   specially) for all cases above except pattern / string.

   For pattern / string, $1, $2, etc. in the replacement string are
   substituted with the string that matched the corresponding
   parenthesized group in the pattern.  If you wish your replacement
   string r to be used literally, use (re-quote-replacement r) as the
   replacement argument.  See also documentation for
   java.util.regex.Matcher's appendReplacement method.

   Example:
   (clojure.string/replace \"Almost Pig Latin\" #\"\\b(\\w)(\\w+)\\b\" \"$2$1ay\")
   -> \"lmostAay igPay atinLay\""
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/replace
  (deftest test-replace
    (is (= "" (str/replace "x" "x" "")) "Replace can produce an empty string")
    (is (= "" (str/replace "x" "x" "")) "Replace can produce an empty string")
    (is (= "" (str/replace "" "x" "x")) "Empty string has nothing to replace")
    (is (= "" (str/replace "" "x" "")) "Empty string has nothing to replace")

    #?@(:cljr [] ;; throws on cljr
        :default
        [(is (= "" (str/replace "" "" "")) "Check for infinite loops")
         (is (= "yxy" (str/replace "x" "" "y")) "Empty string matches between all characters")
         (is (= "yyxyy" (str/replace "x" "" "yy")) "Empty string matches between all characters")])

    (is (= "yy" (str/replace "xx" "x" "y")))
    (is (= "yy" (str/replace "xx" \x \y)))
    (is (= "y" (str/replace "xx" "xx" "y")))
    (is (= "xx" (str/replace "xx" "xxx" "y")))
    (is (= "yyy" (str/replace "yxy" "x" "y")))
    (is (= "yyy" (str/replace "yxy" \x \y)))
    (is (= "yyx" (str/replace "xxxxx" "xx" "y")))
    (is (= "xyz" (str/replace "y" "y" "xyz")))
    (is (= "yyyyy" (str/replace "xyyyx" "x" "y")))
    (is (= "yyyyy" (str/replace "xyyyx" \x \y)))
    (is (= "fooyx" (str/replace "xyxyx" "xyx" "foo")))

    (is (= "" (str/replace "xxx" #".*" "")))

    (testing "Docstring example"
      (is (= "lmostAay igPay atinLay"
             (str/replace "Almost Pig Latin" #"\b(\w)(\w+)\b" "$2$1ay"))))

    (testing "Examples from https://clojuredocs.org/clojure.string/replace"
      (is (= "The color is blue"
             (str/replace "The color is red" #"red" "blue")))
      (is (= "fabulous ddero oo doo"
             (str/replace "fabulous fodder foo food" #"f(o+)(\S+)" "$2$1")))
      (is (= "1 2 1"
             (str/replace "a b a" #"a|b" {"a" "1" "b" "2"})))
      (is (= "1 2 1"
             (str/replace "a b a" #"a|b" {"a" "1" "b" "2"})))
      (is (= "Hello World"
             (str/replace "hello world" #"\b." str/upper-case)))
      (is (= "Hello World"
             (str/replace "hello world" #"\b(.)" (comp str/upper-case first))))
      (is (= "Vegeta"
             (str/replace "Vegeta" #"Goku" "Gohan")))
      (is (= "/my/dir/path"
             (str/replace "/my/dir/path/" #".$" ""))))

    #?(:cljs nil ;; not valid tests for cljs
       :cljr nil ;; not valid tests for cljr
       :default
       (testing "First input does not need to be a string"
         (is (= (str :bar) (str/replace :foo "foo" "bar")))
         (is (= (str ::bar) (str/replace ::foo "foo" "bar")))
         (is (= (str [:bar]) (str/replace [:foo] "foo" "bar")))
         (is (= (str 'bar) (str/replace 'foo "foo" "bar")))
         (is (= (str `bar) (str/replace `foo "foo" "bar")))
         (is (= (str [`bar]) (str/replace [`foo] "foo" "bar")))))

    (testing "Unicode Emoji"
      (is (= "😀" (str/replace "☹️" "☹️" "😀")))
      (is (= "😀😀😀" (str/replace "☹️☹️☹️" "☹️" "😀"))))

    #?(:cljs nil ;; cljs converts chars into strings
       :default
       (testing "Invalid parameter combinations"
         (is (p/thrown? (str/replace nil "x" "y")) "First input must not be nil")
         (is (p/thrown? (str/replace "" "x" \y)) "Match and replacement parameters must match")
         (is (p/thrown? (str/replace "" \x "y")) "Match and replacement parameters must match")
         (is (p/thrown? (str/replace "a" #"." (constantly \1))) "Replacement function must be string")))))