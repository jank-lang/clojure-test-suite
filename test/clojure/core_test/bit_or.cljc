(ns clojure.core-test.bit-or
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability :as p]))

(p/when-var-exists clojure.core/bit-or
  (deftest test-bit-or
    #?(:clj (is (thrown? NullPointerException (bit-or nil 1)))
       :cljs (is (bit-or nil 1)))
    #?(:clj (is (thrown? NullPointerException (bit-or 1 nil)))
       :cljs (is (bit-or 1 nil)))

    (are [ex a b] (= ex (bit-or a b))
      2r1101                   2r1100                   2r1001
      1                        1                        0
      r/all-ones-int           r/all-ones-int           0
      r/all-ones-int           0                        r/all-ones-int
      r/all-ones-int           r/all-ones-int           r/all-ones-int
      r/full-width-checker-pos r/full-width-checker-pos 0
      r/full-width-checker-pos r/full-width-checker-pos r/full-width-checker-pos
      r/all-ones-int           r/full-width-checker-pos r/all-ones-int
      r/all-ones-int           r/full-width-checker-pos r/full-width-checker-neg)))

