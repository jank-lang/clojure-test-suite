(ns clojure.core-test.not-eq
  (:require
   [clojure.core-test.eq :as eq]
   [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]
   [clojure.test :refer [deftest is testing]]))

(when-var-exists clojure.core/not=
  (deftest test-not-eq
    (eq/tests (complement not=))
    ;; There is some arguably buggy behavior in JVM Clojure with not=
    ;; https://ask.clojure.org/index.php/14298/incorrect-result-when-evaluating-not-on-nans
    (testing "If ##NaNs are ="
      #?(:clj (is (not (not= ##NaN ##NaN)))
         :cljs (is (not= ##NaN ##NaN))))))
