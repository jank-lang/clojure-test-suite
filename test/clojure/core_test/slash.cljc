(ns clojure.core-test.slash
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core//
  (deftest test-slash
    (testing "common"
      (are [expected x y] (= expected (/ x y))
        2 2  1
        1 2  2
        3 15 5
        5 15 3

        2 2N  1N
        1 2N  2N
        3 15N 5N
        5 15N 3N

        2 2  1N
        1 2  2N
        3 15 5N
        5 15 3N

        2 2N  1
        1 2N  2
        3 15N 5
        5 15N 3

        2.0 2.0  1.0
        1.0 2.0  2.0
        3.0 15.0 5.0
        5.0 15.0 3.0
        7.5 15.0 2.0

        2.0 2  1.0
        1.0 2  2.0
        3.0 15 5.0
        5.0 15 3.0
        7.5 15 2.0

        2.0 2.0  1
        1.0 2.0  2
        3.0 15.0 5
        5.0 15.0 3
        7.5 15.0 2

        2.0 2.0M  1.0                  ; Unexpected downcast to double
        1.0 2.0M  2.0
        3.0 15.0M 5.0
        5.0 15.0M 3.0
        7.5 15.0M 2.0

        2.0 2.0  1.0M                  ; Unexpected downcast to double
        1.0 2.0  2.0M
        3.0 15.0 5.0M
        5.0 15.0 3.0M
        7.5 15.0 2.0M

        2.0M 2.0M  1.0M
        1.0M 2.0M  2.0M
        3.0M 15.0M 5.0M
        5.0M 15.0M 3.0M
        7.5M 15.0M 2.0M

        2.0M 2  1.0M
        1.0M 2  2.0M
        3.0M 15 5.0M
        5.0M 15 3.0M
        7.5M 15 2.0M

        2.0M 2.0M  1
        1.0M 2.0M  2
        3.0M 15.0M 5
        5.0M 15.0M 3
        7.5M 15.0M 2

        2.0 2N  1.0
        1.0 2N  2.0
        3.0 15N 5.0
        5.0 15N 3.0
        7.5 15N 2.0

        2.0 2.0  1N
        1.0 2.0  2N
        3.0 15.0 5N
        5.0 15.0 3N
        7.5 15.0 2N

        2.0M 2N  1.0M
        1.0M 2N  2.0M
        3.0M 15N 5.0M
        5.0M 15N 3.0M
        7.5M 15N 2.0M

        2.0M 2.0M  1N
        1.0M 2.0M  2N
        3.0M 15.0M 5N
        5.0M 15.0M 3N
        7.5M 15.0M 2N

        ;; test signs
        -1    1     -1
        -1    -1    1
        1     -1    -1
        -1.0  1.0   -1.0
        -1.0  -1.0  1.0
        1.0   -1.0  -1.0
        -1N   1N    -1N
        -1N   -1N   1N
        1N    -1N   -1N
        -1.0M 1.0M  -1.0M
        -1.0M -1.0M 1.0M
        1.0M  -1.0M -1.0M)

      ;; Zero arg
      #?(:cljs nil
         :default (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (/))))

      ;; Single arg
      #?(:cljs (is (= 0.5 (/ 2)))
         :default (is (= 1/2 (/ 2))))
      (is (= 0.5 (/ 2.0)))

      ;; Multi arg
      #?(:cljs (is (= 50 (/ 100 1 2)))
         :default (is (= 1/362880 (/ 1 2 3 4 5 6 7 8 9))))

      #?@(:cljs
          [(is (= ##Inf (/ 1 0)))
           (is (= 0 (/ nil 1)))
           (is (= ##Inf (/ 1 nil)))]
          :default
          [(is (thrown? Exception (/ 1 0)))
           (is (thrown? Exception (/ nil 1)))
           (is (thrown? Exception (/ 1 nil)))]))

    #?(:cljs nil
       :default
       (testing "rationals"
         (are [expected x y] (= expected (/ x y))
           10   10  1
           5    10  2
           10/3 10  3
           1    2   2
           4    2   1/2
           1/4  1/2 2
           4.0   2.0  1/2
           0.25  1/2  2.0
           4M    2.0M 1/2
           0.25M 1/2  2.0M)

         ;; Single arg
         (is (= 2N (/ 1/2)))
         (is (= 3N (/ 1/3)))

         ;; Multi arg
         (is (= 362880N (/ 1/1 1/2 1/3 1/4 1/5 1/6 1/7 1/8 1/9)))

         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (/ 1/2 nil)))
         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (/ nil 1/2)))))

    #?(:bb nil ;; bb always uses boxed args, which always throws in JVM Clojure
       :default
       (testing "inf-nan"
         (are [expected x y] (= expected (/ x y))
           ##Inf  ##Inf  1
           ##-Inf ##-Inf 1
           0.0    1      ##Inf             ; Note conversion to double
           0.0    1      ##-Inf
           0.0    -1     ##Inf
           0.0    -1     ##-Inf
           ##Inf  ##Inf  0             ; Surprisingly, these down't throw
           ##-Inf ##-Inf 0

           ##Inf  ##Inf  1N
           ##-Inf ##-Inf 1N
           0.0    1N     ##Inf             ; Note conversion to double
           0.0    1N     ##-Inf
           0.0    -1N    ##Inf
           0.0    -1N    ##-Inf
           ##Inf  ##Inf  0N            ; Surprisingly, these down't throw
           ##-Inf ##-Inf 0N

           ##Inf  ##Inf  1.0
           ##-Inf ##-Inf 1.0
           0.0    1.0    ##Inf
           0.0    1.0    ##-Inf
           0.0    -1.0   ##Inf
           0.0    -1.0   ##-Inf
           ##Inf  ##Inf  0.0           ; Surprisingly, these down't throw
           ##-Inf ##-Inf 0.0

           ##Inf  ##Inf  1.0M
           ##-Inf ##-Inf 1.0M
           0.0    1.0M    ##Inf            ; Note conversion back double
           0.0    1.0M    ##-Inf
           0.0    -1.0M   ##Inf
           0.0    -1.0M   ##-Inf
           ##Inf  ##Inf  0.0M          ; Surprisingly, these down't throw
           ##-Inf ##-Inf 0.0M

           #?@(:cljs []
               :default
               [0.0 1/2  ##Inf
                0.0 -1/2 ##Inf
                0.0 1/2  ##-Inf
                0.0 -1/2 ##-Inf]))

         ;; These all result in ##NaN, but we can't test for that with `=`.
         (are [x y] (NaN? (/ x y))
           ##NaN  0                        ; Note that this doesn't throw
           0      ##NaN
           ##NaN  0N
           0N     ##NaN
           ##NaN  1.0
           1.0    ##NaN
           ##NaN  1.0M
           1.0M   ##NaN
           #?@(:cljs []
               :default
               [##NaN  1/2
                1/2    ##NaN])
           ##Inf  ##Inf
           ##Inf  ##-Inf
           ##-Inf ##Inf
           ##-Inf ##-Inf)))))
