(ns clojure.core-test.bit-clear
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists bit-clear
  (deftest test-bit-clear
    #?(:cljs (is (= 0 (bit-clear nil 1)))
       :default (is (thrown? Exception (bit-clear nil 1))))
    #?(:cljs (is (= 0 (bit-clear 1 nil)))
       :default (is (thrown? Exception (bit-clear 1 nil))))

    (are [ex a b] (= ex (bit-clear a b))
      3 11 3)))
