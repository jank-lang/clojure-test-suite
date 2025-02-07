(ns clojure.core-test.bit-xor
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-xor
  (deftest test-bit-xor
    #?(:cljs (is (bit-xor nil 1))
       :default (is (thrown? #?(:clj Exception) (bit-xor nil 1))))
    #?(:cljs (is (bit-xor 1 nil))
       :default (is (thrown? #?(:clj Exception) (bit-xor 1 nil))))

    (are [ex a b] (= ex (bit-xor a b))
      2r0101                   2r1100                   2r1001
      #?(:cljs -1 :default r/all-ones-int)           r/all-ones-int           0
      #?(:cljs -1 :default r/all-ones-int)           0                        r/all-ones-int
      0                        r/all-ones-int           r/all-ones-int
      r/full-width-checker-pos r/full-width-checker-pos 0
      0                        r/full-width-checker-pos r/full-width-checker-pos
      #?(:cljs -1431655766 :default r/full-width-checker-neg) r/full-width-checker-pos r/all-ones-int
      #?(:cljs -1 :default r/all-ones-int)           r/full-width-checker-pos r/full-width-checker-neg)))
