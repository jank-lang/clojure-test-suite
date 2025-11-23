(ns clojure.core-test.num
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists num
 (deftest test-num
   #?@(:cljs []
       :clj
       ;; The compiler should pass `x` as a primitive to `num`.
       [(let [x 1.0]
          (is (instance? java.lang.Double (num x))))
        (let [x 1]
          (is (instance? java.lang.Long (num x))))
        ;; `BigInt` and `BigDecimal` are always boxed and `num` just returns them as-is.
        (is (instance? clojure.lang.BigInt (num 1N)))
        (is (instance? java.math.BigDecimal (num 1.0M)))]
       :cljr
       ;; The compiler should pass `x` as a primitive to `num`.
       [(let [x 1.0]
          (is (instance? System.Double (num x))))
        (let [x 1]
          (is (instance? System.Int64 (num x))))
        ;; `BigInt` and `BigDecimal` are always boxed and `num` just returns them as-is.
        (is (instance? clojure.lang.BigInt (num 1N)))
        (is (instance? clojure.lang.BigDecimal (num 1.0M)))]
       :default (is false "TODO test num"))))
