(ns clojure.string-test.upper-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/upper-case
  (deftest test-upper-case
    (is (p/thrown? (str/upper-case nil)))
    (is (= "" (str/upper-case "")))
    (is (= "֎" (str/upper-case "֎")))
    (is (= "ASDF" (str/upper-case "aSDf")))
    (is (= "ASDF" (str/upper-case "ASDF")))
    (let [s "asdf"]
      (is (= "ASDF" (str/upper-case "asdf")))
      (is (= "asdf" s) "original string mutated"))
    ;; Phel's string functions are strict: a non-string argument throws
    ;; rather than being coerced via `str`/`toString` (the JVM `:default`
    ;; behaviour). Documented divergence; matches the :cljs/:lpy/:cljr stance.
    #?(:phel
       (are [v] (p/thrown? (str/upper-case v))
         :asdf
         :asdf/asdf
         'asdf
         'asdf/asdf)

       :cljr
       (are [v] (p/thrown? (str/upper-case v))
         :asdf
         :asdf/asdf
         'asdf
         'asdf/asdf)

       :lpy
       (are [v] (p/thrown? (str/upper-case v))
         :asdf
         :asdf/asdf
         'asdf
         'asdf/asdf)

       :cljs
       (are [v] (p/thrown? (str/upper-case v))
         :asdf
         :asdf/asdf
         'asdf
         'asdf/asdf)

       :default
       (are [expected v] (= expected (str/upper-case v))
         ":ASDF"      :asdf
         ":ASDF/ASDF" :asdf/asdf
         "ASDF"       'asdf
         "ASDF/ASDF"  'asdf/asdf))))
