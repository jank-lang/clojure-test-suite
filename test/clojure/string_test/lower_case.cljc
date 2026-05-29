(ns clojure.string-test.lower-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/lower-case
  (deftest test-lower-case
    (is (p/thrown? (str/lower-case nil)))
    (is (= "" (str/lower-case "")))
    (is (= "֎" (str/lower-case "֎")))
    (is (= "asdf" (str/lower-case "AsdF")))
    (is (= "asdf" (str/lower-case "asdf")))
    (let [s "ASDF"]
      (is (= "asdf" (str/lower-case "ASDF")))
      (is (= "ASDF" s) "original string mutated"))
    ;; Phel's string functions are strict: a non-string argument throws
    ;; rather than being coerced via `str`/`toString` (the JVM `:default`
    ;; behaviour). Documented divergence; matches the :cljs/:lpy/:cljr stance.
    #?(:phel
       (are [v] (p/thrown? (str/lower-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :cljr
       (are [v] (p/thrown? (str/lower-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :lpy
       (are [v] (p/thrown? (str/lower-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)

       :cljs
       (are [v] (p/thrown? (str/lower-case v))
         :ASDF
         :ASDF/ASDF
         'ASDF
         'ASDF/ASDF)
       
       :default
       (are [expected v] (= expected (str/lower-case v))
         ":asdf"      :ASDF
         ":asdf/asdf" :ASDF/ASDF
         "asdf"       'ASDF
         "asdf/asdf"  'ASDF/ASDF))))
