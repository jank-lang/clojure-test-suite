(ns clojure.core-test.pr-str
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists pr-str
 (deftest test-pr-str
   (is (= "\"a\" \"string\"" (pr-str "a" "string")))
   ;; Slight differences in the way that CLJS handles characters and
   ;; numbers with no fractional part.
   (is (= #?(:cljs "nil \"a\" \"string\" \"A\" \" \" 1 17 [:a :b] {:c :d} #{:e}"
             :default "nil \"a\" \"string\" \\A \\space 1 17.0 [:a :b] {:c :d} #{:e}")
          (pr-str nil "a" "string" \A \space 1 17.0 [:a :b] {:c :d} #{:e})))))
