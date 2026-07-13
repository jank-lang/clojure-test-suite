(ns clojure.core-test.bigdec
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists bigdec
  (deftest test-bigdec
    (testing "common cases"
      (are [expected x] (= expected (bigdec x))
        1M 1
        0M 0
        -1M -1
        1M 1N
        0M 0N
        -1M -1N
        1M 1.0
        0M 0.0
        -1M -1.0
        0M "0"
        1M "1"
        1M "+1"                         ; explicit plus sign
        -1M "-1"
        0.5M "0.5"
        -0.5M "-0.5"
        1E+10M "1e10"                   ; exponential notation
        1E+10M "1E10"
        1E+10M "+1e10"
        1E+10M "+1E10"
        -1E+10M "-1e10"
        -1E+10M "-1E10"
        1E+10M "1e+10"
        1E+10M "1E+10"
        1E-10M "1e-10"
        1E-10M "1E-10"
        1E+10M "+1e+10"
        1E+10M "+1E+10"
        1E-10M "+1e-10"
        1E-10M "+1E-10"
        -1E+10M "-1e+10"
        -1E+10M "-1E+10"
        -1E-10M "-1e-10"
        -1E-10M "-1E-10"
        #?@(:cljs []
            :default
            [0.5M  1/2
             0M    0/2
             -0.5M -1/2])
        1.0M 1.0M                       ; identity case
        0M 0M                           ; identity case
        0.0M -0.0
        123456789012345678901234567890.0M 123456789012345678901234567890N
        )

      ;; `bigdec` must produce objects that satisfy `decimal?`
      (is (decimal? (bigdec 1))))

    (testing "exception cases"
      (is (p/thrown? (bigdec ##Inf)))
      (is (p/thrown? (bigdec ##-Inf)))
      (is (p/thrown? (bigdec ##NaN)))
      (is (p/thrown? (bigdec nil)))
      (is (p/thrown? (bigdec "abc")))   ; can't coerce to number
      (is (p/thrown? (bigdec "")))      ; can't coerce to number
      #?@(:cljr
          [(is (= 1.0M (bigdec true)))
           (is (= 0.0M (bigdec false)))]
          :default
          [(is (p/thrown? (bigdec true))) ; type error
           (is (p/thrown? (bigdec false)))])
      (is (p/thrown? (bigdec :a))))))
