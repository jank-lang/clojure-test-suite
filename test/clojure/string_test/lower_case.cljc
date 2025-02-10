(ns clojure.string-test.lower-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/lower-case
  (deftest test-lower-case
    (is (= "" (str/lower-case "")))
    (is (= "asdf" (str/lower-case "ASDF")))
    (is (= "asdf" (str/lower-case "asdf")))
    (is (string? (str/lower-case "asdf")))
    (let [s "ASDF"]
      (is (= "asdf" (str/lower-case "ASDF")))
      (is (= "ASDF" s) "original string mutated"))))
