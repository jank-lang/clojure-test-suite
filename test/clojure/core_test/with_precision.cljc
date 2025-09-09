(ns clojure.core-test.with-precision
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-precision
  (deftest test-with-precision
    ;; tests copied from https://clojuredocs.org/clojure.core/with-precision
    (is (= 2M (with-precision 1 :rounding UP (* 1.1M 1M))))
    (is (= 2M (with-precision 1 :rounding CEILING (* 1.1M 1M))))
    (is (= -2M (with-precision 1 :rounding UP (* -1.1M 1M))))
    (is (= -1M (with-precision 1 :rounding CEILING (* -1.1M 1M))))

    (is (= 1M (with-precision 1 :rounding DOWN (* 1.9M 1M))))
    (is (= 1M (with-precision 1 :rounding FLOOR (* 1.9M 1M))))
    (is (= -1M (with-precision 1 :rounding DOWN (* -1.9M 1M))))
    (is (= -2M (with-precision 1 :rounding FLOOR (* -1.9M 1M))))

    (is (= 2M (with-precision 1 :rounding HALF_EVEN (* 1.5M 1M))))
    (is (= 2M (with-precision 1 :rounding HALF_EVEN (* 2.5M 1M))))
    (is (= -2M (with-precision 1 :rounding HALF_EVEN (* -1.5M 1M))))
    (is (= -2M (with-precision 1 :rounding HALF_EVEN (* -2.5M 1M))))

    (is (= 2M (with-precision 1 :rounding HALF_UP (* 1.5M 1M))))
    (is (= 1M (with-precision 1 :rounding HALF_DOWN (* 1.5M 1M))))
    (is (= -2M (with-precision 1 :rounding HALF_UP (* -1.5M 1M))))
    (is (= -1M (with-precision 1 :rounding HALF_DOWN (* -1.5M 1M))))

    (is (thrown? Exception (with-precision 1 :rounding UNNECESSARY (* 1.5M 1M)))) ;; => Execution error (ArithmeticException) at... Rounding necessary
    (is (= 2M (with-precision 1 :rounding UNNECESSARY (* 2M 1M))))))
