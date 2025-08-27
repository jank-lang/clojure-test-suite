(ns clojure.string-test.lower-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/lower-case
  (deftest test-lower-case
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/lower-case nil)))
    (is (= "" (str/lower-case "")))
    (is (= "֎" (str/lower-case "֎")))
    (is (= "asdf" (str/lower-case "AsdF")))
    (is (= "asdf" (str/lower-case "asdf")))
    (let [s "ASDF"]
      (is (= "asdf" (str/lower-case "ASDF")))
      (is (= "ASDF" s) "original string mutated"))
    #?(:cljs (is (thrown? :default (str/lower-case :ASDF)))
       :default (is (= ":asdf" (str/lower-case :ASDF))))
    #?(:cljs (is (thrown? :default (str/lower-case :ASDF/ASDF)))
       :default (is (= ":asdf/asdf" (str/lower-case :ASDF/ASDF))))
    #?(:cljs (is (thrown? :default (str/lower-case 'ASDF)))
       :default (is (= "asdf" (str/lower-case 'ASDF))))))
