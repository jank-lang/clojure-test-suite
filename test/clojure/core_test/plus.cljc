(ns clojure.core-test.plus
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/+
  (deftest test-+
    (testing "common"
      (are [sum x y] (and (= sum (+ x y))
                          (= sum (+ y x))) ; addition should be commutative
        0  0  0
        1  1  0
        2  1  1
        6  1  5
        10 5  5
        0  1  -1
        -2 -1 -1
        -1 -1 0

        0.0  0.0  0.0
        1.0  1.0  0.0
        2.0  1.0  1.0
        6.0  1.0  5.0
        10.0 5.0  5.0
        0.0  1.0  -1.0
        -2.0 -1.0 -1.0
        -1.0 -1.0 0.0

        0.0  0.0  0
        1.0  1.0  0
        1.0  0.0  1
        2.0  1.0  1
        6.0  1.0  5
        6.0  5.0  1
        10.0 5.0  5
        0.0  1.0  -1
        0.0  -1.0 1
        -2.0 -1.0 -1
        -1.0 -1.0 0
        -1.0 0.0  -1

        0.0  0  0.0
        1.0  1  0.0
        1.0  0  1.0
        2.0  1  1.0
        6.0  1  5.0
        6.0  5  1.0
        10.0 5  5.0
        0.0  1  -1.0
        0.0  -1 1.0
        -2.0 -1 -1.0
        -1.0 -1 0.0
        -1.0 0  -1.0

        1N 0  1N
        1N 0N 1
        1N 0N 1N
        2N 1N 1
        2N 1  1N
        2N 1N 1N
        6N 1  5N
        6N 1N 5
        6N 1N 5N)

      ;; Zero arg
      (is (= 0 (+)))

      ;; One arg
      (is (= 1 (+ 1)))
      (is (= 2 (+ 2)))

      ;; Multi arg
      (is (= 45 (+ 0 1 2 3 4 5 6 7 8 9)))


      #?@(:cljs
          [(is (= 1 (+ 1 nil)))
           (is (= 1 (+ nil 1)))
           (is (= 1 (+ 1N nil)))
           (is (= 1 (+ nil 1N)))
           (is (= 1 (+ 1.0 nil)))
           (is (= 1 (+ nil 1.0)))]
          :default
          [(is (thrown? Exception (+ 1 nil)))
           (is (thrown? Exception (+ nil 1)))
           (is (thrown? Exception (+ 1N nil)))
           (is (thrown? Exception (+ nil 1N)))
           (is (thrown? Exception (+ 1.0 nil)))
           (is (thrown? Exception (+ nil 1.0)))
           (is (thrown? Exception (+ r/max-int 1)))
           (is (thrown? Exception (+ r/min-int -1)))
           (is (instance? clojure.lang.BigInt (+ 0 1N)))
           (is (instance? clojure.lang.BigInt (+ 0N 1)))
           (is (instance? clojure.lang.BigInt (+ 0N 1N)))
           (is (instance? clojure.lang.BigInt (+ 1N 1)))
           (is (instance? clojure.lang.BigInt (+ 1 1N)))
           (is (instance? clojure.lang.BigInt (+ 1N 1N)))
           (is (instance? clojure.lang.BigInt (+ 1 5N)))
           (is (instance? clojure.lang.BigInt (+ 1N 5)))
           (is (instance? clojure.lang.BigInt (+ 1N 5N)))]))

    #?(:cljs nil
       :default
       (testing "rationals"
         (are [sum x y] (and (= sum (+ x y))
                             (= sum (+ y x))) ; addition should be commutative
           1 1/2 1/2
           1 1/3 2/3
           1 1/4 3/4
           1 1/5 4/5
           1 1/6 5/6
           1 1/7 6/7
           1 1/8 7/8
           1 1/9 8/9

           1/2 1 -1/2
           1/3 1 -2/3
           1/4 1 -3/4
           1/5 1 -4/5
           1/6 1 -5/6
           1/7 1 -6/7
           1/8 1 -7/8
           1/9 1 -8/9

           3/2  1 1/2
           5/3  1 2/3
           7/4  1 3/4
           9/5  1 4/5
           11/6 1 5/6
           13/7 1 6/7
           15/8 1 7/8
           17/9 1 8/9

           2 3/2 1/2
           2 4/3 2/3

           ;; Be careful here because floating point rounding can bite us.
           ;; This case is pretty safe.
           1.5 1.0 1/2)

         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (+ 1/2 nil)))
         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (+ nil 1/2)))

         #?@(:cljs nil
             :default
             [(is (+ r/max-int 1/2))    ; test that these don't throw
              (is (+ r/min-int -1/2))
              (is (= r/max-double (+ r/max-double 1/2))) ; should silently round
              (is (= (- r/max-double) (+ (- r/max-double) -1/2)))
              (is (= 0.5 (+ r/min-double 1/2)))
              (is (= -0.5 (+ r/min-double -1/2)))
              (is (instance? clojure.lang.Ratio (+ 0 1/3)))
              (is (instance? clojure.lang.Ratio (+ 0N 1/3)))
              (is (instance? clojure.lang.Ratio (+ 1 1/3)))
              (is (instance? clojure.lang.Ratio (+ 1N 1/3)))
              ;; Note that we use `double?` here because JVM Clojure uses
              ;; java.lang.Double instead of clojure.lang.Double and we'd
              ;; like to keep this test as generic as possible.
              (is (double? (+ 0.0 1/3)))
              (is (double? (+ 1.0 1/3)))])))

    (testing "inf-nan"
      (are [sum x y] (and (= sum (+ x y))
                          (= sum (+ y x))) ; addition should be commutative

        ##Inf  1   ##Inf
        ##Inf  1N  ##Inf
        ##Inf  1.0 ##Inf
        ##-Inf 1   ##-Inf
        ##-Inf 1N  ##-Inf
        ##-Inf 1.0 ##-Inf
        #?@(:cljs []
            :default
            [##Inf  1/2 ##Inf
             ##-Inf 1/2 ##-Inf]))

      (are [x y] (and (NaN? (+ x y))
                      (NaN? (+ y x)))
        ##Inf  ##-Inf
        1      ##NaN
        1N     ##NaN
        1.0    ##NaN
        #?@(:cljs []
            :default
            [1/2    ##NaN])
        ##Inf  ##NaN
        ##-Inf ##NaN
        ##NaN  ##NaN)

      #?@(:cljs
          [(is (NaN? (+ ##NaN nil)))
           (is (= ##Inf (+ ##Inf nil)))]
          :default
          [(is (thrown? Exception (+ ##NaN nil)))
           (is (thrown? Exception (+ ##Inf nil)))]))))
