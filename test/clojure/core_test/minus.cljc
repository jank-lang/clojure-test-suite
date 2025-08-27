(ns clojure.core-test.minus
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/-
  (deftest test--
    (testing "common"
      (are [expected x y] (= expected (- x y))
        ;; longs
        0  0  0
        1  1  0
        -1 -1 0
        0  1  1
        -1 0  1
        -2 -1 1
        2  1  -1
        1  0  -1
        0  -1 -1
        1  2  1
        5  6  1
        5  10 5
        -1 1  2

        ;; doubles
        0.0  0.0  0.0
        1.0  1.0  0.0
        -1.0 -1.0 0.0
        0.0  1.0  1.0
        -1.0 0.0  1.0
        -2.0 -1.0 1.0
        2.0  1.0  -1.0
        1.0  0.0  -1.0
        0.0  -1.0 -1.0
        1.0  2.0  1.0
        5.0  6.0  1.0
        5.0  10.0 5.0
        -1.0 1.0  2.0

        ;; BigInts
        0N  0N  0N
        1N  1N  0N
        -1N -1N 0N
        0N  1N  1N
        -1N 0N  1N
        -2N -1N 1N
        2N  1N  -1N
        1N  0N  -1N
        0N  -1N -1N
        1N  2N  1N
        5N  6N  1N
        5N  10N 5N
        -1N 1N  2N

        ;; BigDecimals
        0.0M  0.0M  0.0M
        1.0M  1.0M  0.0M
        -1.0M -1.0M 0.0M
        0.0M  1.0M  1.0M
        -1.0M 0.0M  1.0M
        -2.0M -1.0M 1.0M
        2.0M  1.0M  -1.0M
        1.0M  0.0M  -1.0M
        0.0M  -1.0M -1.0M
        1.0M  2.0M  1.0M
        5.0M  6.0M  1.0M
        5.0M  10.0M 5.0M
        -1.0M 1.0M  2.0M)

      ;; Zero arg
      #?(:cljs nil
         :default (is (thrown? #?(:cljs :default :clj Exception  :cljr Exception) (-))))

      ;; Single arg
      (is (= -3 (- 3)))
      (is (= 3 (- -3)))

      ;; Multi-arg
      (is (= -45 (- 0 1 2 3 4 5 6 7 8 9)))


      #?@(:cljs
          [(is (= -1 (- nil 1)))
           (is (= 1 (- 1 nil)))
           (is (= -1 (- nil 1N)))
           (is (= 1 (- 1N nil)))
           (is (= -1 (- nil 1.0)))
           (is (= 1 (- 1.0 nil)))
           (is (= -1 (- nil 1.0M)))
           (is (= 1 (- 1.0M nil)))
           ;; Add these back in later
           #_(is (- r/min-int 1))
           #_(is (- r/max-int -1))]
          :default
          [(is (thrown? Exception (- nil 1)))
           (is (thrown? Exception (- 1 nil)))
           (is (thrown? Exception (- nil 1N)))
           (is (thrown? Exception (- 1N nil)))
           (is (thrown? Exception (- nil 1.0)))
           (is (thrown? Exception (- 1.0 nil)))
           (is (thrown? Exception (- nil 1.0M)))
           (is (thrown? Exception (- 1.0M nil)))
           (is (thrown? Exception (- r/min-int 1)))
           (is (thrown? Exception (- r/max-int -1)))]))


    #?(:cljs nil                        ; CLJS doesn't support ratios
       :default
       (testing "rationals"
         (are [expected x y] (= expected (- x y))
           1/2 1 1/2
           1/3 1 2/3
           1/4 1 3/4
           1/5 1 4/5
           1/6 1 5/6
           1/7 1 6/7
           1/8 1 7/8
           1/9 1 8/9

           1 1/2 -1/2
           1 1/3 -2/3
           1 1/4 -3/4
           1 1/5 -4/5
           1 1/6 -5/6
           1 1/7 -6/7
           1 1/8 -7/8
           1 1/9 -8/9

           1  3/2 1/2
           1  5/3 2/3
           1  7/4 3/4
           1  9/5 4/5
           1 11/6 5/6
           1 13/7 6/7
           1 15/8 7/8
           1 17/9 8/9

           3/2 2 1/2
           4/3 2 2/3

           ;; Be careful here because floating point rounding can bite us.
           ;; This case is pretty safe.
           1.0 1.5 1/2)

         ;; Single arg
         (is (= -1/2 (- 1/2)))
         (is (= 1/2 (- -1/2)))

         ;; Multi arg
         (is (= -2089/2520 (- 1 1/2 1/3 1/4 1/5 1/6 1/7 1/8 1/9)))

         (is (thrown? Exception (- nil 1/2)))
         (is (thrown? Exception (- 1/2 nil)))

         (is (- r/max-int -1/2))        ; test that these don't throw
         (is (- r/min-int 1/2))
         (is (= (- r/max-double) (- (- r/max-double) 1/2))) ; should silently round
         (is (= r/max-double (- r/max-double -1/2)))
         (is (= -0.5 (- r/min-double 1/2)))
         (is (= 0.5 (- r/min-double -1/2)))
         (is (instance? clojure.lang.Ratio (- 0 1/3)))
         (is (instance? clojure.lang.Ratio (- 0N 1/3)))
         (is (instance? clojure.lang.Ratio (- 1 1/3)))
         (is (instance? clojure.lang.Ratio (- 1N 1/3)))
         ;; Note that we use `double?` here because JVM Clojure uses
         ;; java.lang.Double instead of clojure.lang.Double and we'd
         ;; like to keep this test as generic as possible.
         (is (double? (- 0.0 1/3)))
         (is (double? (- 1.0 1/3)))))

    (testing "inf-nan"
      (are [expected x y] (= expected (- x y))
        ##Inf  1   ##-Inf
        ##Inf  1N  ##-Inf
        ##-Inf 1   ##Inf
        ##-Inf 1N  ##Inf
        ##Inf  1.0 ##-Inf
        ##-Inf 1.0 ##Inf
        #?@(:cljs []
            :default
            [##Inf  1/2 ##-Inf
             ##-Inf 1/2 ##Inf])

        ##-Inf ##-Inf 1
        ##-Inf ##-Inf 1N
        ##-Inf ##-Inf 1.0
        ##Inf  ##Inf  1
        ##Inf  ##Inf  1N
        ##Inf  ##Inf  1.0
        #?@(:cljs []
            :default
            [##-Inf ##-Inf 1/2
             ##Inf  ##Inf  1/2]))

      (are [x y] (and (NaN? (- x y))
                      (NaN? (- y x)))
        ##-Inf ##-Inf
        1      ##NaN
        1N     ##NaN
        #?@(:cljs []
            :default
            [1.0    ##NaN
             1/2    ##NaN])
        ##Inf  ##NaN
        ##-Inf ##NaN
        ##NaN  ##NaN)

      ;; Single arg
      (is (= ##-Inf (- ##Inf)))
      (is (= ##Inf (- ##-Inf)))
      (is (NaN? (- ##NaN)))

      #?@(:cljs
          ;; CLJS treats nil as zero in arithemetic operations
          [(is (NaN? (- ##NaN nil)))
           (is (= ##Inf (- ##Inf nil)))]
          :default
          [(is (thrown? Exception (- ##NaN nil)))
           (is (thrown? Exception (- ##Inf nil)))]))))
