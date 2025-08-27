(ns clojure.core-test.bit-test
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/bit-test
  (deftest test-bit-test
    #?(:cljs (is (= false (bit-test nil 1)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-test nil 1))))
    #?(:cljs (is (= true (bit-test 1 nil)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (bit-test 1 nil))))

    (are [ex a b] (= ex (bit-test a b))
      true  2r1001 0
      false 2r1001 1
      false 2r1001 2
      true  2r1001 3
      false 2r1001 4
      false 2r1001 63)))
