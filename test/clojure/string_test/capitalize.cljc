(ns clojure.string-test.capitalize
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists str/capitalize
  (deftest test-capitalize
    (is (p/thrown? (str/capitalize nil)))
    #?(:cljs (do (is (p/thrown? (str/capitalize 1)))
                 (is (p/thrown? (str/capitalize 'a)))
                 (is (p/thrown? (str/capitalize 'a/a)))
                 (is (p/thrown? (str/capitalize :a)))
                 (is (p/thrown? (str/capitalize :a/a))))
       :cljr (do (is (p/thrown? (str/capitalize 1)))
                 (is (p/thrown? (str/capitalize 'Asdf)))
                 (is (p/thrown? (str/capitalize 'asDf/aSdf)))
                 (is (p/thrown? (str/capitalize :asDf/aSdf))))
       :lpy (do (is (p/thrown? (str/capitalize 1)))
                (is (p/thrown? (str/capitalize 'Asdf)))
                (is (p/thrown? (str/capitalize 'asDf/aSdf)))
                (is (p/thrown? (str/capitalize :asDf/aSdf))))
       :default (do (is (= "1" (str/capitalize 1)))
                    (is (= "Asdf" (str/capitalize 'Asdf)))
                    (is (= "Asdf/asdf" (str/capitalize 'asDf/aSdf)))
                    (is (= ":asdf/asdf" (str/capitalize :asDf/aSdf)))))
    (is (= "" (str/capitalize "")))
    (is (= "A" (str/capitalize "a")))
    (is (= "֎" (str/capitalize "֎")))
    (is (= "A thing" (str/capitalize "a Thing")))
    (is (= "A thing" (str/capitalize "A THING")))
    (is (= "A thing" (str/capitalize "A thing")))))
