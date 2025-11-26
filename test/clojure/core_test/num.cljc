(ns clojure.core-test.num
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(defn f [])

#?(:bb
   nil

   :clj
   (do
    (definterface IChecker
      (isLong [^long l])
      (isLong [^Object l])

      (isDouble [^double d])
      (isDouble [^Object d]))

   (deftype Checker []
     IChecker
     (isLong [_this ^long _l] true)
     (isLong [_this ^Object _l] false)
     (isDouble [_this ^double _d] true)
     (isDouble [_this ^Object _d] false))))

(when-var-exists num
 (deftest test-num
   (testing "positive cases"
     #?@(:bb
         [(is (NaN? (num ##NaN)))
          (are [n] (and (= n (num n))
                        (= (type n) (type (num n))))
            0
            0.1
            1/2
            1N
            1.0M
            (short 1)
            (byte 1)
            (int 1)
            (long 1)
            (float 1.0)
            (double 1.0)
            nil
            ##Inf)]

         :cljs
         []

         :clj
         [(testing "longs"
            (let [l       (long 1)
                  L       (num l)
                  checker (Checker.)]
              (is (.isLong checker l))
              (is (false? (.isLong checker L)))))
          (testing "doubles"
            (let [d       (double 1.0)
                  D       (num d)
                  checker (Checker.)]
              (is (.isDouble checker d))
              (is (false? (.isDouble checker D)))))
          ;; `BigInt` and `BigDecimal` are always boxed and `num` just returns them as-is.
          (is (instance? clojure.lang.BigInt (num 1N)))
          (is (instance? java.math.BigDecimal (num 1.0M)))]

         :cljr [(is (NaN? (num ##NaN)))
                (is (= (byte 1) (num (byte 1))))
                (is (= System.UInt64 (type (num (byte 1)))))
                (are [n] (and (= n (num n))
                              (= (type (num n)) System.Int64))
                  (short 1)
                  (int 1))
                (are [n] (and (= n (num n))
                              (= (type n) (type (num n))))
                  0
                  0.1
                  1/2
                  1N
                  1.0M
                  (long 1)
                  (float 1.0)
                  (double 1.0)
                  nil
                  ##Inf)]

         ;; By default assume that other platforms are no-ops for numeric inputs
         :default [(is (NaN? (num ##NaN)))
                   (are [n] (and (= n (num n))
                                 (= (type n) (type (num n))))
                     0
                     0.1
                     1/2
                     1N
                     1.0M
                     (short 1)
                     (byte 1)
                     (int 1)
                     (long 1)
                     (float 1.0)
                     (double 1.0)
                     nil
                     ##Inf)])
     (testing "exceptions thrown"
       ;; [[num]] is *almost* a true no-op in `cljr`, equivalent to [[identity]],
       ;; except that it will upcast to System.Int64/System.UInt64
       #?@(:cljs
           []

           :cljr
           [(are [x] (and (= x (num x))
                          (= (type x) (type (num x))))
              f
              {}
              #{}
              []
              '()
              \1
              \a
              ""
              "1"
              'a
              #"")
            (is (fn? (num (fn []))))]

           :default
           [(are [x] (thrown? Exception (num x))
              (fn [])
              f
              {}
              #{}
              []
              '()
              \1
              \a
              ""
              "1"
              'a
              #"")])))))
