(ns clojure.core-test.short
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists short
  (deftest test-short
    ;; There is no platform independent predicate to test for a
    ;; short (e.g., `short?`). In ClojureJVM, it's an instance of
    ;; `java.lang.Short`, but there is no predicate for it. Here, we just
    ;; test whether it's a fixed-length integer of some sort.
    (is (int? (short 0)))
    #?@(:cljs []
        :default
        [(is (instance? #?(:clj java.lang.Short :cljr System.Int16) (short 0)))])

    ;; Check conversions and rounding from other numeric types
    (are [expected x] (= expected (short x))
      -32768 -32768
      0    0
      32767 32767
      1    1N
      0    0N
      -1   -1N
      1    1.0M
      0    0.0M
      -1   -1.0M
      #?@(:cljs ; short is a dummy cast in CLJS
          [1.1    1.1
           -1.1   -1.1
           1.9    1.9]
          :default
          [1    1.1
           -1   -1.1
           1    1.9])
      #?@(:cljs []
          :default
          [1    3/2
           -1   -3/2
           0    1/10
           0    -1/10])
      #?@(:cljs
          [1.1    1.1M
           -1.1   -1.1M]
          :default
          [1    1.1M
           -1   -1.1M]))

    #?@(:cljs
        [;; CLJS short is just a dummy cast
         (is (= -32768.1 (short -32768.1)))
         (is (= -32769 (short -32769)))
         (is (= 32768 (short 32768)))
         (is (= 32767.1 (short 32767.1)))
         (is (= "0" (short "0")))
         (is (= :0 (short :0)))
         (is (= [0] (short [0])))
         (is (= nil (short nil)))]
        :cljr
        [;; `short` throws outside the range of 32767 ... -32768.
         (is (= (short -32768) (short -32768.000001)))
         (is (thrown? Exception (short -32769)))
         (is (thrown? Exception (short 32768)))
         (is (= (short 32767) (short 32767.000001)))

         ;; Check handling of other types
         (is (= (short 0) (short "0")))
         (is (thrown? Exception (short :0)))
         (is (thrown? Exception (short [0])))
         (is (thrown? Exception (short nil)))]

        :default
        [;; `short` throws outside the range of 32767 ... -32768.
         #?@(:bb []
             :clj
             [(is (thrown? Exception (short -32768.000001)))
              (is (thrown? Exception (short -32769)))
              (is (thrown? Exception (short 32768)))
              (is (thrown? Exception (short 32767.000001)))])

         ;; Check handling of other types
         (is (thrown? Exception (short "0")))
         (is (thrown? Exception (short :0)))
         (is (thrown? Exception (short [0])))
         (is (thrown? Exception (short nil)))])))
