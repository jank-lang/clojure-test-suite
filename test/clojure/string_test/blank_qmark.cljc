(ns clojure.string-test.blank-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/blank?
  (deftest test-blank?
    (is (true? (str/blank? "")))
    (is (true? (str/blank? nil)))
    (is (false? (str/blank? "֎")))
    (testing "U+2007"
      (is (#?(:cljs true? :cljr true :default false?) (str/blank? " ")))
      (is (#?(:cljs true? :cljr true :default false?) (str/blank? "\u2007"))))
    (is (true? (str/blank? "  ")))
    (is (true? (str/blank? " \t ")))
    #?(:cljs (do (is (true? (str/blank? (symbol ""))))
                 (is (false? (str/blank? 'a))))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/blank? (symbol "")))))
    #?(:cljs (do (is (false? (str/blank? (keyword ""))))
                 (is (false? (str/blank? :a))))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/blank? (keyword "")))))
    #?(:cljs (is (false? (str/blank? 1)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/blank? 1))))
    #?(:cljs (do (is (true? (str/blank? \space)))
                 (is (false? (str/blank? \a))))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/blank? \space))))
    (is (false? (str/blank? "nil")))
    (is (false? (str/blank? " as df ")))))
