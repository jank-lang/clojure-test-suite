(ns clojure.string-test.split-lines
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/split-lines
  (deftest test-split-lines
    (is (= [""] (str/split-lines "")))
    (is (= [] (str/split-lines "\n")))
    (is (= [] (str/split-lines "\r\n")))
    (is (= ["" "bar"] (str/split-lines "\nbar")))
    (is (= ["foo"] (str/split-lines "foo\n")))
    (is (= ["foo" "bar"] (str/split-lines "foo\nbar")))
    (is (p/thrown? (str/split-lines nil)))
    (is (p/thrown? (str/split-lines \A)))
    (is (p/thrown? (str/split-lines 0)))
    (is (p/thrown? (str/split-lines 0.0)))
    (is (p/thrown? (str/split-lines :foo)))
    (is (p/thrown? (str/split-lines 'foo)))
    (is (p/thrown? (str/split-lines [])))))
