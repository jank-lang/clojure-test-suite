(ns clojure.core-test.seq
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists seq
 (deftest test-seq
   ;; Sourced via canSeq https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/RT.java#L581
   (are [in expected] (= expected (seq in))
     "test" '(\t \e \s \t)
     [1 2 3 4] '(1 2 3 4)
     '(:a :b :c :d) '(:a :b :c :d)
     '() nil
     nil nil
     (sorted-set 3.0 1.0 -2.5 4.0) '(-2.5 1.0 3.0 4.0)
     (range 5 10) '(5 6 7 8 9)
     #?@(:cljs [(int-array 3) '(nil nil nil)]
         :default [(int-array 3) '(0 0 0)]))
   (testing "sets and maps"
     (let [input #{440M 55000M 80000}
           input-hash (into (hash-set) input)
           input-map {:a {:b "4"}
                      :c 800
                      nil 40}
           input-sorted-map (into (sorted-map) input-map)
           input-hash-map (into (hash-map) input-map)]
       (is (= input (into #{} (seq input))))
       (is (= input-hash (into (hash-set) (seq input))))
       (is (= input-sorted-map (into (sorted-map) (seq input-sorted-map))))
       (is (= input-hash-map (into (hash-map) (seq input-hash-map))))
       (is (= input-map (into {} (seq input-map))))))
   (testing "nonseqables"
     (is (thrown? #?(:cljs :default :default Exception) (seq 1)))
     (is (thrown? #?(:cljs :default :default Exception) (seq (fn [])))))
   (testing "infinite sequences are produced by seq"
     (let [infinite-seq (seq (range))]
       (is (seq? infinite-seq))
       (is (= (range 10000) (take 10000 infinite-seq)))))))
