(ns clojure.core-test.bit-and
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-and
  (deftest test-bit-and
    #?(:cljs (is (= 0 (bit-and nil 1)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-and nil 1))))
    #?(:cljs (is (= 0 (bit-and 1 nil)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-and 1 nil))))

    (are [ex a b] (= ex (bit-and a b))
      8                        12                       9
      8                        8                        0xff
      0                        r/all-ones-int           0
      0                        0                        r/all-ones-int
      #?(:cljs -1 :default r/all-ones-int)           r/all-ones-int           r/all-ones-int
      0                        r/full-width-checker-pos 0
      r/full-width-checker-pos r/full-width-checker-pos r/full-width-checker-pos
      r/full-width-checker-pos r/full-width-checker-pos r/all-ones-int
      0                        r/full-width-checker-pos r/full-width-checker-neg)))
