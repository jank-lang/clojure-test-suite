(ns clojure.core-test.double
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/double
 (deftest test-double
   (are [expected x] (= expected (double x))
     (double 1.0) 1
     (double 0.0) 0
     (double -1.0) -1
     (double 1.0) 1N
     (double 0.0) 0N
     (double -1.0) -1N
     (double 1.0) 12/12
     (double 0.0) 0/12
     (double -1.0) -12/12
     (double 1.0) 1.0M
     (double 0.0) 0.0M
     (double -1.0) -1.0M)
   (is (NaN? (double ##NaN)))

   #?@(:cljs
       ;; In cljs, `double` is just returns the argument unchanged (dummy fn)
       [(is (= "0" (double "0")))
        (is (= :0 (double :0)))]
       :jank []
	   :cljr
       [(is (= 0.0 (double "0")))
        (is (thrown? Exception (double :0)))]
       :default
       [(is (thrown? Exception (double "0")))
        (is (thrown? Exception (double :0)))])

   #?@(:clj
       [(is (instance? java.lang.Double (double 0)))
        (is (instance? java.lang.Double (double 0.0)))
        (is (instance? java.lang.Double (double 0N)))
        (is (instance? java.lang.Double (double 0.0M)))]
	   :cljr
       [(is (instance? System.Double (double 0)))
        (is (instance? System.Double (double 0.0)))
        (is (instance? System.Double (double 0N)))
        (is (instance? System.Double (double 0.0M)))])))
