(ns clojure.core-test.group-by
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists group-by
  (deftest test-group-by
    (testing "basics"
      (is (= {false [0 2 4 6 8] true [1 3 5 7 9]} (group-by odd? (range 10))))
      (is (= {0 [0], 1 [1], 2 [2], 3 [3], 4 [4], 5 [5], 6 [6], 7 [7], 8 [8], 9 [9]}
             (group-by identity (range 10))))
      ;; Empty sequence returns empty map
      (is (= {} (group-by odd? '())))
      (is (= {} (group-by odd? nil))))

    (testing "preservation of item order"

      )

    #?(:phel nil ;; phel does not propagate metadata through group-by
       :default
       (testing "metadata"
         ;; Metadata shouldn't participate in the comparison
         (let [g (group-by first [[^:foo [1] [2]] [^:bar [1] [3]]])]
           (is (= {[1] [[[1] [2]] [[1] [3]]]} g))
           ;; Metadata should be preserved on grouped items
           (is (= {:foo true} (-> g (get [1]) first first meta)))
           (is (= {:bar true} (-> g (get [1]) second first meta))))

         ;; Metadata should also be preserved on items and items should
         ;; stay in the order of the initial collection
         (let [s (group-by empty? [^:a [] ^:b [1] ^:c [] ^:d [2]])]
           ;; validate grouping
           (is (= s {true [[] []] false [[1] [2]]}))
           ;; validate order of items in each grouping using attached
           ;; metadata, which also validates that metadata is preserved
           ;; on items
           (is (= {:a true} (-> s (get true) first meta)))
           (is (= {:c true} (-> s (get true) second meta)))
           (is (= {:b true} (-> s (get false) first meta)))
           (is (= {:d true} (-> s (get false) second meta))))))))
