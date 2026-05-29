(ns clojure.core-test.parse-boolean
  (:require [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

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
      ;; Phel's `parse-boolean` is intentionally nil-safe: it returns `nil` for
      ;; any non-string input (and any string other than "true"/"false")
      ;; instead of throwing, so it can be chained in `when`/`if-let` without
      ;; guarding. Documented divergence.
      #?(:phel (are [x] (nil? (parse-boolean x))
                 nil
                 0
                 0.0
                 :key
                 {}
                 '()
                 #{}
                 [])
         :lpy (are [x] (p/thrown? (parse-boolean x))
                nil
                0
                0.0
                :key
                {}
                '()
                #{}
                [])
         :cljs (are [x] (p/thrown? (parse-boolean x))
                 nil
                 0
                 0.0
                 :key
                 {}
                 '()
                 #{}
                 [])
         :default (are [x] (p/thrown? (parse-boolean x))
                    nil
                    0
                    0.0
                    \a
                    :key
                    {}
                    '()
                    #{}
                    [])))))
