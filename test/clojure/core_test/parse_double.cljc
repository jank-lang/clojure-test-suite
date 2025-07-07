(ns clojure.core-test.parse-double
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/parse-double
  (deftest test-parse-double
    (testing "common"
      (are [expected x] (= expected (parse-double x))
           nil ""
           nil "foo"
           nil "f00"
           nil "7oo"
           nil "four"
           nil "##Inf"
           nil "-##Inf"
           nil "+-5.6"
           nil "-+5.6"
           nil "7.9+6.4"
           nil "7.9-6.4"
           nil "Infinity7"
           nil "-50Infinity7"
           nil "8-Infinity7"
           nil "InfinityE100"
           nil "Infinitye5"
           nil "2.6e8E5"
           1.0 "1"
           1.0 "1.0"
           1.0 "1.000"
           5.6 "+5.6"
           -8.7 "-8.7"
           90006.0 "9.0006e4"
           -105.8 "-1.058e2"
           568.51 "56851e-2"
           568.51 "56851E-2"
           ##Inf "Infinity"
           ##-Inf "-Infinity"))
    (testing "exceptions"
      #?(:clj (are [x] (thrown? Exception (parse-double x))
                   {}
                   '()
                   []
                   #{}
                   \a
                   :key
                   0.0
                   1000))
      #?(:cljs (are [x] (thrown? js/Error (parse-double x))
                   {}
                   '()
                   []
                   #{}
                   :key
                   0.0
                   1000)))))
