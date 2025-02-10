(ns clojure.string-test.blank-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/blank?
  (deftest test-blank?
    (is (str/blank? ""))
    (is (str/blank? nil))
    (is (str/blank? "  "))
    (is (str/blank? " \t "))
    (is (not (str/blank? "nil")))
    (is (not (str/blank? " as df ")))))
