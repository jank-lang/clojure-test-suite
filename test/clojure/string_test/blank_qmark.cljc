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
    #?(:cljs (do (is (str/blank? (symbol "")))
                 (is (not (str/blank? 'a))))
       :default (is (thrown? #?(:clj Exception) (str/blank? (symbol "")))))
    #?(:cljs (do (is (not (str/blank? (keyword ""))))
                 (is (not (str/blank? :a))))
       :default (is (thrown? #?(:clj Exception) (str/blank? (keyword "")))))
    #?(:cljs (is (not (str/blank? 1)))
       :default (is (thrown? #?(:clj Exception) (str/blank? 1))))
    #?(:cljs (do (is (str/blank? \space))
                 (is (str/blank? \a)))
       :default (is (thrown? #?(:clj Exception) (str/blank? \space))))
    (is (not (str/blank? "nil")))
    (is (not (str/blank? " as df ")))))
