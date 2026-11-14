(ns clojure.string-test.includes-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.string/includes?

  ;; DOCSTRING:
  ;; clojure.string/includes?
  ;; [s substr]
  ;; True if s includes substr.

  (deftest test-includes?
    (testing "empty strings"
      (is (true? (str/includes? "" "")))
      (is (true? (str/includes? "a-test" "")))
      (is (false? (str/includes? "" "a"))))

    (testing "substring search"
      (are [expected s substr] (= expected (str/includes? s substr))
        true  "a-test" "a"
        true  "a-test" "a-test"
        true  "a-test" "-"
        true  "a-test" "test"
        true  "aaaa"   "aa"
        true  "a.b"    "."
        true  "a[b"    "["
        false "a-test" "x"
        false "a-test" "b"
        false "a-test" "tse"
        false "a-test" "a-test-"
        false "a-test" "A-test"
        false "a"      "aa"
        false "a-test" " "
        false "axb"    "."))

    (testing "non-ASCII text"
      (is (true? (str/includes? "▒a-test" "▒")))
      (is (true? (str/includes? "a-test▒" "▒")))
      (is (false? (str/includes? "a-test" "▒"))))

    (testing "nil arguments"
      #?(:cljs (do (is (false? (str/includes? "" nil)))
                   (is (false? (str/includes? "a-test" nil)))
                   (is (p/thrown? (str/includes? nil "")))
                   (is (p/thrown? (str/includes? nil nil))))
         :default (do (is (p/thrown? (str/includes? "" nil)))
                      (is (p/thrown? (str/includes? "a-test" nil)))
                      (is (p/thrown? (str/includes? nil "")))
                      (is (p/thrown? (str/includes? nil nil))))))))
