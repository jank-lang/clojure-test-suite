(ns clojure.core-test.group-by
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists group-by
  (deftest test-group-by
    ;; Basics
    (is (= {false [0 2 4 6 8] true [1 3 5 7 9]} (group-by odd? (range 10))))
    (is (= {0 [0], 1 [1], 2 [2], 3 [3], 4 [4], 5 [5], 6 [6], 7 [7], 8 [8], 9 [9]}
           (group-by identity (range 10))))
    ;; Empty sequence returns empty map
    (is (= {} (group-by odd? '())))
    (is (= {} (group-by odd? nil)))))
