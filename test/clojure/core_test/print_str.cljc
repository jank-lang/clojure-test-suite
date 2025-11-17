(ns clojure.core-test.print-str
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists print-str
 (deftest test-print-str
   (is (= "a string" (print-str "a" "string")))
   (is (= #?(:cljs "nil a string A   1 17 [:a :b] {:c :d} #{:e}"
             :default "nil a string A   1 17.0 [:a :b] {:c :d} #{:e}")
          (print-str nil "a" "string" \A \space 1 17.0 [:a :b] {:c :d} #{:e})))))
