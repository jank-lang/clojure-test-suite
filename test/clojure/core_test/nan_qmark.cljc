(ns clojure.core-test.nan-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/NaN?
 (deftest test-NaN?
   (is (thrown? #?(:cljs :default :default Exception) (NaN? nil)))
   (is (thrown? #?(:cljs :default :default Exception) (NaN? "##NaN")))
   (is (double? ##NaN))
   ;; NaN is not equal to anything, even itself.
   ;; See: https://clojure.org/guides/equality
   ;; Note that we use `(not (= ...))` rather than `(not= ...)` because
   ;; of a bug in clojure.core.
   (is (not (= ##NaN ##NaN)))
   (are [in ex] (= ex (NaN? in))
     0             false
     1.0           false
     -1.0          false
     (double 1.0)  false
     (double -1.0) false
     ##Inf         false
     ##-Inf        false
     ##NaN         true)))
