(ns clojure.core-test.bit-flip
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-flip
  (deftest test-bit-flip
    #?(:cljs (is (= 2 (bit-flip nil 1)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-flip nil 1))))
    #?(:cljs (is (= 0 (bit-flip 1 nil)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-flip 1 nil))))

    (are [ex a b] (= ex (bit-flip a b))
      2r1111 2r1011 2
      2r1011 2r1111 2)))
