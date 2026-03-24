(ns clojure.core-test.bit-clear
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists bit-clear
  (deftest test-bit-clear
    #?(:cljs (is (= 0 (bit-clear nil 1)))
       :default (is (p/thrown? (bit-clear nil 1))))
    #?(:cljs (is (= 0 (bit-clear 1 nil)))
       :default (is (p/thrown? (bit-clear 1 nil))))

    (are [ex a b] (= ex (bit-clear a b))
      3 11 3)))
