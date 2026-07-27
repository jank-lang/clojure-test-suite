(ns clojure.string-test.split-lines
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/split-lines
  (deftest test-split-lines
    (testing "Normal use"
      #?(:cljr (is (= [] (str/split-lines "")))
         :default (is (= [""] (str/split-lines ""))))
      (is (= [] (str/split-lines "\n")))
      (is (= [] (str/split-lines "\r\n")))
      (is (= ["" "bar"] (str/split-lines "\nbar")))
      (is (= ["foo"] (str/split-lines "foo\n")))
      (is (= ["foo" "bar"] (str/split-lines "foo\nbar")))
      (is (= ["foo " " bar"] (str/split-lines "foo \n bar")))
      (is (= ["foo"] (str/split-lines "foo\n\n"))))

    #?(:cljs nil
       :default
       (testing "Exceptions"
         (is (p/thrown? (str/split-lines nil)))
         (is (p/thrown? (str/split-lines \A)))
         (is (p/thrown? (str/split-lines 0)))
         (is (p/thrown? (str/split-lines 0.0)))
         (is (p/thrown? (str/split-lines :foo)))
         (is (p/thrown? (str/split-lines 'foo)))
         (is (p/thrown? (str/split-lines [])))))))
