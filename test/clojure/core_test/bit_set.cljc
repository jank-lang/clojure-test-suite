(ns clojure.core-test.bit-set
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-set
  (deftest test-bit-set
    #?(:cljs (is (bit-set nil 1))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-set nil 1))))
    #?(:cljs (is (bit-set 1 nil))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-set 1 nil))))

    (are [ex a b] (= ex (bit-set a b))
      2r1111               2r1011 2
      #?@(:cljs [] :default [-9223372036854775808 0 63])
      #?@(:cljs [] :default [4294967296           0 32])
      65536                0      16
      256                  0      8
      16                   0      4)))
