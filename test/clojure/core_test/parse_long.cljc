(ns clojure.core-test.parse-long
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/parse-long
  (deftest test-parse-long
    (testing "common"
      (are [expected x] (= expected (parse-long x))
           nil                ""
           nil                "1L"
           nil                "foo"
           nil                "f00"
           nil                "7oo"
           nil                "four"
           nil                "0.0"
           nil                "+-5"
           nil                "-+5"
           nil                "##Inf"
           nil                "-##Inf"
           nil                "Infinity"
           nil                "Infinity7"
           nil                "-Infinity"
           nil                "-50Infinity"
           nil                "NaN"
           0                  "0"
           42                 "42"
           12                 "+12"
           -1000              "-1000"
           -100000000000      "-100000000000"
           #?@(:clj  [999999999999999999 "999999999999999999"]
               :cljs [nil                "999999999999999999"
                      999999999999999    "999999999999999"])))
    (testing "exceptions"
      #?(:clj (are [x] (thrown? Exception (parse-long x))
                   {}
                   '()
                   []
                   #{}
                   \a
                   :key
                   0.0
                   1000))
      #?(:cljs (are [x] (thrown? js/Error (parse-long x))
                   {}
                   '()
                   []
                   #{}
                   :key
                   0.0
                   1000)))))
