(ns clojure.core-test.bit-not
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists bit-not
  (deftest test-bit-not
    #?(:cljs (is (= -1 (bit-not nil)))
       :default (is (thrown? Exception (bit-not nil))))

    (are [ex a] (= ex (bit-not a))
      -2r1000 2r0111
      2r0111  -2r1000)))
