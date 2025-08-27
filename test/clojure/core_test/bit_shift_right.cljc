(ns clojure.core-test.bit-shift-right
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-shift-right
  (deftest test-bit-shift-right
    #?(:cljs (is (bit-shift-right nil 1))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-shift-right nil 1))))
    #?(:cljs (is (bit-shift-right 1 nil))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-shift-right 1 nil))))

    (are [ex a b] (= ex (bit-shift-right a b))
      2r1101 2r1101 0
      2r110  2r1101 1
      2r11   2r1101 2
      2r1    2r1101 3
      2r0    2r1101 4
      2r0    2r1101 63)))
