(ns clojure.core-test.get-in
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists get-in
  (deftest test-get-in
    (testing "common"
      (is (= nil (get-in nil nil)))
      (is (= nil (get-in nil nil "not found")))

      ;; maps
      (is (= {} (get-in {} nil)))
      (is (= {} (get-in {} nil "not found")))
      (is (= {} (get-in {} [])))
      (is (= {} (get-in {} [] "not found")))
      (is (= "not found" (get-in {} [nil] "not found")))
      (is (= \space (get-in {} [:a] \space)))
      (is (= 0 (get-in {} ["a"] 0)))
      (is (= 1 (get-in {:a 1} [:a])))
      (is (= {:a 1} (get-in {:a 1} nil)))
      (is (= {:a 1} (get-in {:a 1} [])))
      (is (= {:a 1} (get-in {:a 1} '())))
      (is (= {:b {:c :d}} (get-in {:a {:b {:c :d}}} [:a])))
      (is (= {:c :d} (get-in {:a {:b {:c :d}}} [:a :b])))
      (is (= :d (get-in {:a {:b {:c :d} :e (range)}} [:a :b :c])))
      (is (= nil (get-in {:a {:b {:c :d}}} [:a :b :c :d])))
      (is (= :d (get-in {:a {:b {:c :d} :e (range)}} [:a :b :c :d] :d)))

      ;; vectors
      (is (= 0 (get-in [[0 1 2]
                        [3 4 5]
                        [6 7 8]] [0 0])))
      (is (= 4 (get-in [[0 1 2]
                        [3 4 (range)]
                        [6 7 8]] [1 1])))
      (is (= 9 (get-in [[0 1 2]
                        [3 4 5]
                        [6 7 (range)]] [2 3] 9)))
      
      ;; maps and vector mix 
      (is (= "x" (get-in {:id 1
                          :matrix [[0 1 2]
                                   [3 4 {:var "x"}]
                                   [6 7 8]]}
                         
                         [:matrix 1 2 :var] "y"))))))
