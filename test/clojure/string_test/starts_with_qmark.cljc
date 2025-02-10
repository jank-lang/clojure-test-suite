(ns clojure.string-test.starts-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/starts-with?
  (deftest test-starts-with?
    (is (str/starts-with? "" ""))
    (is (str/starts-with? "a-test" "a"))
    (is (not (str/starts-with? "a-test" "b")))))
