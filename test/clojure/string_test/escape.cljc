(ns clojure.string-test.escape
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/escape
  (deftest test-escape
    (is (= "" (str/escape "" {})))
    (is (= "" (str/escape "" {\c "C_C"})))
    (is (= "A_Abc" (str/escape "abc" {\a "A_A"})))
    (is (= "A_AbC_C" (str/escape "abc" {\a "A_A" \c "C_C"})))
    (is (= "A_AbC_C" (str/escape "abc" {\a "A_A" \c "C_C" (int \a) 1 nil 'junk :garbage 42.42})))
    (is (= "A_AbC_C" (str/escape "abc" {\a "A_A" \c "C_C"})))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/escape nil {\a "A_A" \c "C_C"})))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/escape 1 {\a "A_A" \c "C_C"})))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/escape 'a {\a "A_A" \c "C_C"})))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (str/escape :a {\a "A_A" \c "C_C"})))))
