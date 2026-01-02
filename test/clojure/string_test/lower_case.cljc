(ns clojure.string-test.lower-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/lower-case
  (deftest test-lower-case
    (is (thrown? #?(:cljs :default :default Exception) (str/lower-case nil)))
    (is (= "" (str/lower-case "")))
    (is (= "֎" (str/lower-case "֎")))
    (is (= "asdf" (str/lower-case "AsdF")))
    (is (= "asdf" (str/lower-case "asdf")))
    (let [s "ASDF"]
      (is (= "asdf" (str/lower-case "ASDF")))
      (is (= "ASDF" s) "original string mutated"))
    #?(:cljs
       (are [v] (thrown? :default (str/upper-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :cljr
       (are [v] (thrown? Exception (str/upper-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :lpy
       (are [v] (thrown? Exception (str/upper-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :default
       (are [expected v] (= expected (str/upper-case v))
         ":asdf"      :ASDF
         ":asdf/asdf" :ASDF/ASDF
         "asdf"       'ASDF
         "asdf/asdf"  'ASDF/ASDF))))
