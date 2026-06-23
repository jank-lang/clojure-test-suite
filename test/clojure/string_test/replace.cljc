(ns clojure.string-test.replace
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/replace
  (deftest test-replace
    (is (p/thrown? (str/replace nil "x" "y")) "Input must be string")

    (is (= "" (str/replace "x" "x" "")) "Replace can produce an empty string")
    (is (= "" (str/replace "" "x" "x")) "Empty string has nothing to replace")
    (is (= "" (str/replace "" "x" "")) "Empty string has nothing to replace")
    #?(:cljr
       (is true "ClojureCLR doesn't support empty strings for match")
       :default
       (do (is (= "" (str/replace "" "" "")) "Check for infinite loops")
           (is (= "yxy" (str/replace "x" "" "y")) "Empty string matches between all characters")
           (is (= "yyxyy" (str/replace "x" "" "yy")) "Empty string matches between all characters")))

    (is (= "yy" (str/replace "xx" "x" "y")))
    (is (= "y" (str/replace "xx" "xx" "y")))
    (is (= "xx" (str/replace "xx" "xxx" "y")))
    (is (= "yyy" (str/replace "yxy" "x" "y")))
    (is (= "yyx" (str/replace "xxxxx" "xx" "y")))
    (is (= "yyyyy" (str/replace "xyyyx" "x" "y")))))