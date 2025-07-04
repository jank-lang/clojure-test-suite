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
           1.0 "1"
           1.0 "1.0"
           1.0 "1.000"
           5.6 "+5.6"
           -8.7 "-8.7"
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
