(ns clojure.core-test.interpose
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists
 interpose
 (deftest test-interpose
   (testing "common cases"
     (is (fn? (interpose "a")))
     (are [in ex] (= (apply interpose in) ex)
       ["a" [1 2 3]]              [1 "a" 2 "a" 3]
       [#{1} #{"a"}]              ["a"]
       [(sorted-set 1 2 3)
        (sorted-set "a" "b" "c")] ["a" (sorted-set 1 2 3)
                                   "b" (sorted-set 1 2 3)
                                   "c"]
       ["a"
        (sorted-map :a 1 :b 2)]   [[:a 1] "a" [:b 2]]
       [1 '("a" "b" "c")]         ["a" 1 "b" 1 "c"]
       ["1"    "abc"]             [\a "1" \b "1" \c]
       [[:a]     [nil]]           [nil])
     (testing "nil inputs"
       (are [in ex] (= (apply interpose in) ex)
         [nil [1 2 3]]            [1 nil 2 nil 3]
         [1   nil]                '())))))
