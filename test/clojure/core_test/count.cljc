(ns clojure.core-test.count
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/count
  (deftest test-count
    (are [expected x] (= expected (count x))
      0 nil
      0 '()
      0 []
      0 {}
      0 #{}
      0 ""
      1 '(:a)
      1 [:a]
      1 {:a 1}
      1 #{:a}
      1 "a"
      2 '(:a :b)
      2 [:a :b]
      2 {:a 1 :b 2}
      2 #{:a :b}
      2 "ab")

    ;; Negative tests
    (are [x] (thrown? #?(:cljs :default :clj Exception) (count x))
      1
      :a
      'a
      #?@(:cljs [] :default [\a]))))
