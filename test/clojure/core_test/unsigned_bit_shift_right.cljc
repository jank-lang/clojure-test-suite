(ns clojure.core-test.unsigned-bit-shift-right
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/unsigned-bit-shift-right
 (deftest test-unsigned-bit-shift-right
   #?(:cljs (is (= 0 (unsigned-bit-shift-right nil 1)))
      :default (is (thrown? #?(:clj Exception) (unsigned-bit-shift-right nil 1))))
   #?(:cljs (is (= 1 (unsigned-bit-shift-right 1 nil)))
      :default (is (thrown? #?(:clj Exception) (unsigned-bit-shift-right 1 nil))))

   (are [ex a b] (= ex (unsigned-bit-shift-right a b))
     ;; Clojure JVM starts with a 64-bit -1 and ClojureScript starts
     ;; with a 32-bit -1
     #?@(:cljs
         [4194303 -1 10]
         :default
         [18014398509481983 -1 10]))))
