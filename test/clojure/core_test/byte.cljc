(ns clojure.core-test.byte
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/byte
  (deftest test-byte
    ;; There is no platform independent predicate to test for a
    ;; byte (e.g., `byte?`). In ClojureJVM, it's an instance of
    ;; `java.lang.Byte`, but there is no predicate for it. Here, we just
    ;; test whether it's a fixed-length integer of some sort.
    (is (int? (byte 0)))
    #?(:clj  (is (instance? java.lang.Byte (byte 0)))
	   :cljr (is (instance? System.Byte (byte 0))))

    ;; Check conversions and rounding from other numeric types
    (are [expected x] (= expected (byte x))
      -128 -128
      0    0
      127  127
      1    1N
      0    0N
      -1   -1N
      1    1.0M
      0    0.0M
      -1   -1.0M
      ;; Clojurescript `byte` is a "dummy cast" which doesn't do
      ;; anything (no-op). Thus, there is no conversion, no truncation
      ;; of decimal values, etc.
      #?@(:cljs
          [1.1    1.1
           -1.1   -1.1
           1.9    1.9
           1.1    1.1M
           -1.1   -1.1M]
          :default
          [1    1.1
           -1   -1.1
           1    1.9
           1    3/2
           -1   -3/2
           0    1/10
           0    -1/10
           1    1.1M
           -1   -1.1M]))

    #?@(:cljs
        [ ;; ClojureScript `byte` just returns its argument
         (is (= -128.5 (byte -128.5)))
         (is (= -129 (byte -129)))
         (is (= 128(byte 128)))
         (is (= 127.5 (byte 127.5)))
         (is (= "0" (byte "0")))
         (is (= :0 (byte :0)))
         (is (= [0] (byte [0])))
         (is (= nil (byte nil)))]
        :bb [] ;; byte constructions goes via boxed argument
        :default
        [ ;; `byte` throws outside the range of 127 ... -128.
         (is (thrown? #?(:clj Exception :cljr Exception) (byte -128.000001)))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte -129)))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte 128)))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte 127.000001)))
         ;; Check handling of other types
         (is (thrown? #?(:clj Exception :cljr Exception) (byte "0")))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte :0)))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte [0])))
         (is (thrown? #?(:clj Exception :cljr Exception) (byte nil)))])))
