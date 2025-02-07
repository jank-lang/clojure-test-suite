(ns clojure.core-test.abs
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/abs
  (deftest test-abs
    (testing "common"
     (are [in ex] (= ex (abs in))
       -1             1
       1              1
       (inc r/min-int)      (- (inc r/min-int))
       #?@(:clj [r/min-int r/min-int])  ; fixed int 2's complement oddity
       -1.0           1.0
       -0.0           0.0
       ##-Inf         ##Inf
       ##Inf          ##Inf
       -123.456M      123.456M
       -123N          123N
       #?@(:cljs []
           :default
           [-1/5           1/5]))
     (is (NaN? (abs ##NaN)))
     #?(:cljs (is (zero? (abs nil)))
        :default (is (thrown? #?(:clj Exception) (abs nil)))))

    (testing "unboxed"
      (let [a  42
            b  -42
            a' (abs a)
            b' (abs b)]
        (is (= 42 a'))
        (is (= 42 b'))))))
