(ns clojure.core-test.parse-boolean
  (:require [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists parse-boolean
  (deftest test-parse-boolean
    (testing "common"
      (are [expected x] (= expected (parse-boolean x))
           false "false"
           true  "true"
           nil   "0"
           nil   "1"
           nil   ""
           nil   "foo"
           nil   "False"
           nil   "FALSE"
           nil   "True"
           nil   "TRUE"
           nil   "ttrue"
           nil   "truee"
           nil   "ffalse"
           nil   "falsee"
           nil   " true"
           nil   "tr ue"
           nil   "true "))

    (testing "exceptions"
      #?(:cljs (are [x] (thrown? js/Error (parse-boolean x))
                 nil
                 0
                 0.0
                 :key
                 {}
                 '()
                 #{}
                 [])
         :default (are [x] (thrown? Exception (parse-boolean x))
                    nil
                    0
                    0.0
                    \a
                    :key
                    {}
                    '()
                    #{}
                    [])))))
