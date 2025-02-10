(ns clojure.string-test.ends-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/ends-with?
  (deftest test-ends-with?
    (is (str/ends-with? "" ""))
    (is (str/ends-with? "a-test" "t"))
    (is (not (str/ends-with? "a-test" "s")))))
