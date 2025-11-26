(ns clojure.core-test.num
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]])
  (:import #?(:cljr [Assigner AssignObject])))

(defn f [])

#?(:clj
   (do
    (definterface IChecker
      (isLong [^long l])
      (isLong [^Object l])

      (isDouble [^double d])
      (isDouble [^Object d]))

   (deftype Checker []
     IChecker
     (isLong [this ^long l] true)
     (isLong [this ^Object l] false)
     (isDouble [this ^double d] true)
     (isDouble [this ^Object d] false))))

(when-var-exists num
 (deftest test-num
   (testing "positive cases"
   #?@(:cljs []
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
       ;; By default assume that other platforms are no-ops for numeric inputs
       :default (are [n] (and (= n (num n))
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
                  ##NaN
                  ##Inf))
   (testing "exceptions thrown"
     ;; [[num]] is a true no-op in `cljr`, equivalent to [[identity]]
     #?(:cljs
         nil

         :cljr
         (are [x] (and (= x (num x))
                       (= (type x) (type (num x))))
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
           #"")

         :default
         (are [x] (thrown? Exception (num x))
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
           #""))))))
