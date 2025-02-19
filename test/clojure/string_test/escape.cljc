(ns clojure.string-test.escape
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/escape
  (deftest test-escape
    (is (= "" (str/escape "" {})))
    (is (= "" (str/escape "" {\a "A_A"})))
    (is (= "" (str/escape "" {\c "C_C"})))
    (is (= "" (str/escape "" {\a "A_A" \c "C_C"})))
    (is (= "A_AbC_C" (str/escape "abc" {\a "A_A" \c "C_C"})))))
