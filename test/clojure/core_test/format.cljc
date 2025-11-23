(ns clojure.core-test.format
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists format
;;; Note that `format` presents a bit of a conundrum for
;;; testing. Clojure JVM delegates the formatting task to
;;; Java. ClojureScript doesn't implement `format`. Other Clojure
;;; implementations may take different paths. Even when `format` is
;;; implemented, the full scope of Java's `java.util.Formatter`
;;; functionality may not be there. Thus, we take a very conservative
;;; approach here for the default case of just testing to verify that
;;; the function exists and that a simple format string with no escape
;;; characters passes through `format` unharmed.
;;; See: https://clojurians.slack.com/archives/C03SRH97FDK/p1733853098700809

 (deftest test-format
   #?@(:cljs [] ; CLJS doesn't have `format`
       :default
       [(is (= "test" (format "test")))])))
