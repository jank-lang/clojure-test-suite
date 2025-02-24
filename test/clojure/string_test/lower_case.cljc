(ns clojure.string-test.lower-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/lower-case
  (deftest test-lower-case
    (is (thrown? #?(:clj Exception) (str/lower-case nil)))
    (is (= "" (str/lower-case "")))
    (is (= "asdf" (str/lower-case "AsdF")))
    (is (= "asdf" (str/lower-case "asdf")))
    (let [s "ASDF"]
      (is (= "asdf" (str/lower-case "ASDF")))
      (is (= "ASDF" s) "original string mutated"))
    (is (= ":asdf" (str/upper-case :ASDF)))
    (is (= ":asdf/asdf" (str/upper-case :ASDF/ASDF)))
    (is (= "sdf" (str/upper-case 'ASDF)))))
