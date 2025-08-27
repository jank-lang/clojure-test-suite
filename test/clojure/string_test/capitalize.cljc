(ns clojure.string-test.capitalize
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/capitalize
  (deftest test-capitalize
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/capitalize nil)))
    #?(:cljs (do (is (thrown? :default (str/capitalize 1)))
                 (is (thrown? :default (str/capitalize 'a)))
                 (is (thrown? :default (str/capitalize 'a/a)))
                 (is (thrown? :default (str/capitalize :a)))
                 (is (thrown? :default (str/capitalize :a/a))))
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
