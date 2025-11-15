(ns clojure.core-test.prn-str
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists prn-str
 (deftest test-prn-str
   (let [nl (prn-str)]
     ;; `nl` grabs the platform specific newline without calling out to
     ;; platform-specific code (e.g., Java) since we want this to run
     ;; in all environments. Note that we're not actually testing
     ;; whether the newline sequence itself is correct, only that
     ;; `prn-str` adds it to the end of the string.
     (is (= (str "\"a\" \"string\"" nl) (prn-str "a" "string")))
     (is (= #?(:cljs (str "nil \"a\" \"string\" \"A\" \" \" 1 17 [:a :b] {:c :d} #{:e}" nl)
               :default (str "nil \"a\" \"string\" \\A \\space 1 17.0 [:a :b] {:c :d} #{:e}" nl))
            (prn-str nil "a" "string" \A \space 1 17.0 [:a :b] {:c :d} #{:e}))))))
