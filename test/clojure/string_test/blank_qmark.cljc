(ns clojure.string-test.blank-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/blank?
  (deftest test-blank?
    (is (str/blank? ""))
    (is (str/blank? nil))
    (is (str/blank? "  "))
    (is (str/blank? " \t "))
    #?(:cljs (is (str/blank? (symbol "")))
       :default (is (thrown? #?(:clj Exception) (str/blank? (symbol "")))))
    #?(:cljs (is (not (str/blank? (keyword ""))))
       :default (is (thrown? #?(:clj Exception) (str/blank? (keyword "")))))
    #?(:cljs (is (not (str/blank? 1)))
       :default (is (thrown? #?(:clj Exception) (str/blank? 1))))
    #?(:cljs (is (str/blank? \space))
       :default (is (thrown? #?(:clj Exception) (str/blank? \space))))
    (is (not (str/blank? "nil")))
    (is (not (str/blank? "nil")))
    (is (not (str/blank? " as df ")))))
