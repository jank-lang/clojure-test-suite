(ns clojure.core-test.char
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/char
 (deftest test-char
   (are [expected x] (= expected (char x))
     ;; Assumes ASCII / Unicode
     \space 32
     \@     64
     \A     65
     \A     \A
     ;; TODO: Add Unicode tests
     )

   #?(:cljs nil :default (is (thrown? #?(:clj Exception :cljr Exception) (char -1))))
   (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (char nil)))))
