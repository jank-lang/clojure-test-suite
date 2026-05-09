(ns clojure.core-test.star-squote
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.number-range :as r]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists big-int?] :as p]))

(when-var-exists *'
  (deftest test-*'
    (are [prod cand er] (= prod (*' cand er))
      0 0 0
      0 1 0
      0 0 1
      1 1 1
      5 1 5
      5 5 1
      25 5 5
      -1 1 -1
      -1 -1 1
      1 -1 -1
      0 -1 0
      0 0 -1
      (inc r/min-int) r/max-int -1
      (inc r/min-int) -1 r/max-int

      0.0 0.0 0.0
      0.0 1.0 0.0
      0.0 0.0 1.0
      1.0 1.0 1.0
      5.0 1.0 5.0
      5.0 5.0 1.0
      25.0 5.0 5.0
      -1.0 1.0 -1.0
      -1.0 -1.0 1.0
      1.0 -1.0 -1.0
      0.0 -1.0 0.0
      0.0 0.0 -1.0

      0.0 0.0 0
      0.0 1.0 0
      0.0 0.0 1
      1.0 1.0 1
      5.0 1.0 5
      5.0 5.0 1
      25.0 5.0 5
      -1.0 1.0 -1
      -1.0 -1.0 1
      1.0 -1.0 -1
      0.0 -1.0 0
      0.0 0.0 -1

      0.0 0 0.0
      0.0 1 0.0
      0.0 0 1.0
      1.0 1 1.0
      5.0 1 5.0
      5.0 5 1.0
      25.0 5 5.0
      -1.0 1 -1.0
      -1.0 -1 1.0
      1.0 -1 -1.0
      0.0 -1 0.0
      0.0 0 -1.0

      0 0 1N
      0 0N 1
      0 0N 1N
      1 1N 1
      1 1 1N
      1 1N 1N
      5 1 5N
      5 1N 5
      5 1N 5N)

    (is (p/thrown? (*' 1 nil)))
    (is (p/thrown? (*' nil 1)))

    (is (big-int? (*' 0 1N)))
    (is (big-int? (*' 0N 1)))
    (is (big-int? (*' 0N 1N)))
    (is (big-int? (*' 1N 1)))
    (is (big-int? (*' 1 1N)))
    (is (big-int? (*' 1N 1N)))
    (is (big-int? (*' 1 5N)))
    (is (big-int? (*' 1N 5)))
    (is (big-int? (*' 1N 5N)))

    (is (big-int? (*' -1 r/min-int)))
    (is (big-int? (*' r/min-int -1)))
    (is (big-int? (*' (long (/ r/min-int 2)) 3)))
    (is (big-int? (*' 3 (long (/ r/min-int 2)))))))
