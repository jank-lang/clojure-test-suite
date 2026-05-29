(ns clojure.core-test.even-qmark
  (:require [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists even?
  (deftest test-even?
    (testing "common"
      (are [in ex] (= (even? in) ex)
        0     true
        -0    true
        12    true
        17    false
        -118  true
        -119  false
        123N  false
        122N  true
        -121N false
        -120N true))

    (testing "invalid"
      ;; Phel's `even?` is intentionally lenient with non-integer numbers:
      ;; instead of throwing it returns `false` for floats and infinities
      ;; (##Inf/##-Inf/##NaN/1.5/0.2M/1/2). Only `nil` throws. Documented
      ;; divergence.
      #?(:phel (do
                 (are [x] (p/thrown? (even? x))
                   nil)
                 (are [x] (= false (even? x))
                   ##Inf
                   ##-Inf
                   ##NaN
                   1.5
                   0.2M
                   1/2))
         :default (are [x] (p/thrown? (even? x))
                    nil
                    ##Inf
                    ##-Inf
                    ##NaN
                    1.5
                    0.2M
                    #?@(:cljs    []
                        :default [1/2]))))))
