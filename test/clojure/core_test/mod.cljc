(ns clojure.core-test.mod
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists]
             :as p]))

(when-var-exists mod
  (deftest test-mod
    (are [type-pred expected x y] (let [r (mod x y)]
                                    (and (type-pred r)
                                         (= expected r)))
      int? 1  10  3
      int? 2  -10 3
      int? -1 -10 -3
      int? -2 10  -3

      ;; Note that CLJS reads big integers as doubles with the
      ;; fractional part equal to zero (just like CLJS ints). The
      ;; big-int? function is conditionalized for CLJS.
      p/big-int? 1N  10  3N
      p/big-int? 2N  -10 3N
      p/big-int? -1N -10 -3N
      p/big-int? -2N 10  -3N

      p/big-int? 1N  10N  3
      p/big-int? 2N  -10N 3
      p/big-int? -1N -10N -3
      p/big-int? -2N 10N  -3

      p/big-int? 1N  10N  3N
      p/big-int? 2N  -10N 3N
      p/big-int? -1N -10N -3N
      p/big-int? -2N 10N  -3N

      double? 1.0  10    3.0
      double? 2.0  -10   3.0
      double? -1.0 -10   -3.0
      double? -2.0 10    -3.0
      double? 1.0  10.0  3
      double? 2.0  -10.0 3
      double? -1.0 -10.0 -3
      double? -2.0 10.0  -3
      double? 1.0  10.0  3.0
      double? 2.0  -10.0 3.0
      double? -1.0 -10.0 -3.0
      double? -2.0 10.0  -3.0

      ;; CLJS can read big decimals at the reader, but just converts
      ;; them to doubles.
      #?@(:cljs
          [double? 1.0M  10     3.0M
           double? 2.0M  -10    3.0M
           double? -1.0M -10    -3.0M
           double? -2.0M 10     -3.0M
           double? 1.0M  10.0M  3
           double? 2.0M  -10.0M 3
           double? -1.0M -10.0M -3
           double? -2.0M 10.0M  -3
           double? 1.0M  10.0M  3.0M
           double? 2.0M  -10.0M 3.0M
           double? -1.0M -10.0M -3.0M
           double? -2.0M 10.0M  -3.0M]
          :default
          [decimal? 1.0M  10     3.0M
           decimal? 2.0M  -10    3.0M
           decimal? -1.0M -10    -3.0M
           decimal? -2.0M 10     -3.0M
           decimal? 1.0M  10.0M  3
           decimal? 2.0M  -10.0M 3
           decimal? -1.0M -10.0M -3
           decimal? -2.0M 10.0M  -3
           decimal? 1.0M  10.0M  3.0M
           decimal? 2.0M  -10.0M 3.0M
           decimal? -1.0M -10.0M -3.0M
           decimal? -2.0M 10.0M  -3.0M])

      ;; Unexpectedly downconverts result to double, rather than BigDecimal
      double? 1.0  10.0M  3.0
      double? 2.0  -10.0M 3.0
      double? -1.0 -10.0M -3.0
      double? -2.0 10.0M  -3.0
      double? 1.0  10.0   3.0M
      double? 2.0  -10.0  3.0M
      double? -1.0 -10.0  -3.0M
      double? -2.0 10.0   -3.0M

      #?@(:cljs []
          :default [p/big-int? 0N    3     1/2
                    p/big-int? 0N    3     -1/2
                    p/big-int? -1N   3     -4/3
                    p/big-int? 0N    -3    1/2
                    p/big-int? 1N    -3    4/3
                    p/big-int? 0N    -3    -1/2
                    ratio?     1/3   3     4/3
                    ratio?     7/2   37/2  15
                    ratio?     -23/2 37/2  -15
                    ratio?     23/2  -37/2 15
                    ratio?     -1/3  -3    -4/3
                    ratio?     -7/2  -37/2 -15]))

    #?@(:cljs
        [(is (NaN? (mod 10 0)))
         (is (NaN? (mod ##Inf 1)))
         (is (NaN? (mod 1 ##Inf)))
         (is (NaN? (mod ##-Inf 1)))
         (is (NaN? (mod 1 ##-Inf)))
         (is (NaN? (mod ##NaN 1)))
         (is (NaN? (mod 1 ##NaN)))
         (is (NaN? (mod ##NaN 1)))]
        :default
        [(is (thrown? Exception (mod 10 0)))
         (is (thrown? Exception (mod ##Inf 1)))
         (is (NaN? (mod 1 ##Inf)))
         (is (thrown? Exception (mod ##-Inf 1)))
         (is (NaN? (mod 1 ##-Inf)))
         (is (thrown? Exception (mod ##NaN 1)))
         (is (thrown? Exception (mod 1 ##NaN)))
         (is (thrown? Exception (mod ##NaN 1)))])))
