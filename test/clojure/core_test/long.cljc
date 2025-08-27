(ns clojure.core-test.long
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/long
  (deftest test-long
    ;; There is no platform independent predicate to test specifically
    ;; for a long. In ClojureJVM, it's an instance of `java.lang.Long`,
    ;; but there is no predicate for it. Here, we just test whether it's
    ;; a fixed-length integer of some sort.
    (is (int? (int 0)))
    #?(:clj (is (instance? java.lang.Long (long 0))))

    ;; Check conversions and rounding from other numeric types
    (are [expected x] (= expected (long x))
      -9223372036854775808 -9223372036854775808
      0    0
      9223372036854775807 9223372036854775807
      1    1N
      0    0N
      -1   -1N
      1    1.0M
      0    0.0M
      -1   -1.0M
      1    1.1
      -1   -1.1
      1    1.9
      #?@(:cljs []
          :default
          [1    3/2
           -1   -3/2
           0    1/10
           0    -1/10])
      1    1.1M
      -1   -1.1M)

    #?@(:cljs []              ; In CLJS all numbers are really doubles
        :default
        ;; `long` throws outside the range of 9223372036854775807 ... -9223372036854775808
        [(is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (long -9223372036854775809)))
         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (long 9223372036854775808)))])

    ;; Check handling of other types
    #?@(:cljs
        [(is (= 0 (long "0")))          ; JavaScript peeking through?
         (is (NaN? (long :0)))
         (is (NaN? (long [0])))
         (is (= 0 (long nil)))]         ; Hm. Interesting.
        :cljr
        [(is (= 0 (long "0")))
         (is (thrown? Exception (long :0)))
         (is (thrown? Exception (long [0])))
         (is (thrown? Exception (long nil)))]		 
        :default
        [(is (thrown? Exception (long "0")))
         (is (thrown? Exception (long :0)))
         (is (thrown? Exception (long [0])))
         (is (thrown? Exception (long nil)))])))
