(ns clojure.core-test.float
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists float
  (deftest test-float
    (are [expected x] (= expected (float x))
      (float 1.0) 1
      (float 0.0) 0
      (float -1.0) -1
      (float 1.0) 1N
      (float 0.0) 0N
      (float -1.0) -1N
      ;; The CLJS reader will read these values and convert to
      ;; double. Since they are all clean conversions, they match
      (float 1.0) 12/12
      (float 0.0) 0/12
      (float -1.0) -12/12
      (float 1.0) 1.0M
      (float 0.0) 0.0M
      (float -1.0) -1.0M
      ;; Since CLJS numbers are all doubles, casting r/min-double to a
      ;; float doesn't do anything, whereas in Clojure JVM it rounds
      ;; down to zero.
      #?@(:cljs [r/min-double r/min-double]
          :default [(float 0.0) r/min-double]))
    (is (NaN? (float ##NaN)))

    #?@(:cljs
        [(is (= r/max-double (float r/max-double)))
         (is (= ##Inf (float ##Inf)))
         (is (= ##-Inf (float ##-Inf)))
         (is (= "0" (float "0")))
         (is (= :0 (float :0)))]
        :cljr
        [(is (thrown? Exception (float r/max-double)))
         (is (thrown? Exception (float ##Inf)))
         (is (thrown? Exception (float ##-Inf)))
         (is (= (float 0.0) (float "0")))
         (is (thrown? Exception(float :0)))]
        :default
        [(is (thrown? Exception (float r/max-double)))
         (is (thrown? Exception (float ##Inf)))
         (is (thrown? Exception (float ##-Inf)))
         (is (thrown? Exception (float "0")))
         (is (thrown? Exception (float :0)))])

    #?@(:clj
        [(is (instance? java.lang.Float (float 0)))
         (is (instance? java.lang.Float (float 0.0)))
         (is (instance? java.lang.Float (float 0N)))
         (is (instance? java.lang.Float (float 0.0M)))]
       :cljr
        [(is (instance? System.Single (float 0)))
         (is (instance? System.Single (float 0.0)))
         (is (instance? System.Single (float 0N)))
         (is (instance? System.Single (float 0.0M)))])))
