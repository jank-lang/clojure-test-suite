(ns clojure.core-test.bit-shift-left
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-shift-left
  (deftest test-bit-shift-left
    #?(:cljs (is (= 0 (bit-shift-left nil 1)))
       :default (is (thrown? #?(:clj Exception) (bit-shift-left nil 1))))
    #?(:cljs (is (= 1 (bit-shift-left 1 nil)))
       :default (is (thrown? #?(:clj Exception) (bit-shift-left 1 nil))))

    (are [ex a b] (= ex (bit-shift-left a b))
      1024     1      10
      2r110100 2r1101 2)))
