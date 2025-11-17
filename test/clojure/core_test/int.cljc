(ns clojure.core-test.int
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists int
  (deftest test-int
    ;; There is no platform independent predicate to test specifically
    ;; for an int. While `int?` exists, it returns true for any
    ;; fixed-range integer type (e.g., byte, short, int, or long). In
    ;; ClojureJVM, it's an instance of `java.lang.Integer`, but there is no
    ;; predicate for it. Here, we just test whether it's a fixed-length
    ;; integer of some sort.
    (is (int? (int 0)))
    #?(:clj  (is (instance? java.lang.Integer (int 0)))
       :cljr (is (instance? System.Int32 (int 0))))

    ;; Check conversions and rounding from other numeric types
    (are [expected x] (= expected (int x))
      -2147483648 -2147483648
      0    0
      2147483647 2147483647
      1    1N
      0    0N
      -1   -1N
      1    1.0M
      0    0.0M
      -1   -1.0M
      1    1.1
      -1   -1.1
      1    1.9
      1    1.1M
      -1   -1.1M
      #?@(:cljs []
          :default
          [1    3/2
           -1   -3/2
           0    1/10
           0    -1/10]))

    #?@(:cljs []
        :bb []
        :cljr
        [ ;; `int` throws outside the range of 32767 ... -32768.
         (is (thrown? Exception (int -2147483648.000001)))
         (is (thrown? Exception (int -2147483649)))
         (is (thrown? Exception (int 2147483648)))
         (is (thrown? Exception (int 2147483647.000001)))

         ;; Check handling of other types
         (is (= 0 (int "0")))
         (is (thrown? Exception (int :0)))
         (is (thrown? Exception (int [0])))
         (is (thrown? Exception (int nil)))]
        :default
        [ ;; `int` throws outside the range of 32767 ... -32768.
         (is (thrown? Exception (int -2147483648.000001)))
         (is (thrown? Exception (int -2147483649)))
         (is (thrown? Exception (int 2147483648)))
         (is (thrown?  Exception (int 2147483647.000001)))

         ;; Check handling of other types
         (is (thrown? Exception (int "0")))
         (is (thrown? Exception (int :0)))
         (is (thrown? Exception (int [0])))
         (is (thrown? Exception (int nil)))])))
