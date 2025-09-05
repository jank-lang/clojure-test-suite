(ns clojure.string-test.upper-case
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/upper-case
  (deftest test-upper-case
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/upper-case nil)))
    (is (= "" (str/upper-case "")))
    (is (= "֎" (str/upper-case "֎")))
    (is (= "ASDF" (str/upper-case "aSDf")))
    (is (= "ASDF" (str/upper-case "ASDF")))
    (let [s "asdf"]
      (is (= "ASDF" (str/upper-case "asdf")))
      (is (= "asdf" s) "original string mutated"))
    #?(:cljs (is (thrown? :default (str/upper-case :asdf)))
	   :cljr (is (thrown? Exception (str/upper-case :asdf)))
       :default (is (= ":ASDF" (str/upper-case :asdf))))
    #?(:cljs (is (thrown? :default (str/upper-case :asdf/asdf)))
	   :cljr (is (thrown? Exception (str/upper-case :asdf/asdf)))
       :default (is (= ":ASDF/ASDF" (str/upper-case :asdf/asdf))))
    #?(:cljs (is (thrown? :default (str/upper-case 'asdf)))
	   :cljr (is (thrown? Exception (str/upper-case 'asdf)))
       :default (is (= "ASDF" (str/upper-case 'asdf))))
    #?(:cljs (is (thrown? :default (str/upper-case 'asdf/asdf)))
	   :cljr (is (thrown? Exception (str/upper-case 'asdf/asdf)))
       :default (is (= "ASDF/ASDF" (str/upper-case 'asdf/asdf))))))
