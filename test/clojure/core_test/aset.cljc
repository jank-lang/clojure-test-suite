(ns clojure.core-test.aset
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/aset

  ;; DOCSTRING:
  ;; clojure.core/aset
  ;; [array idx val]
  ;; [array idx idx2 & idxv]
  ;; Sets the value at the index/indices. Works on Java arrays of
  ;; reference types. Returns val.

  (deftest test-aset
    (testing "arity-3 stores the value at the index and returns it"
      (let [a (object-array [:a :a :a])]
        (is (= :b (aset a 1 :b)))
        (is (= [:a :b :a] (vec a)))))

    (testing "every index can be set"
      ;; only the addressed slot changes
      (let [a (object-array [1 2 3])]
        (aset a 0 :x)
        (is (= [:x 2 3] (vec a))))
      (let [a (object-array [1 2 3])]
        (aset a 1 :x)
        (is (= [1 :x 3] (vec a))))
      (let [a (object-array [1 2 3])]
        (aset a 2 :x)
        (is (= [1 2 :x] (vec a)))))

    (testing "the value is stored as-is"
      (testing "nil and false are genuine values, not absences"
        (let [a (object-array [1 2])]
          (is (nil? (aset a 0 nil)))
          (is (nil? (aget a 0))))
        (let [a (object-array [true true])]
          (is (false? (aset a 1 false)))
          (is (false? (aget a 1)))))
      (testing "the value itself is stored, not a copy"
        (let [v (object-array [1 2])
              a (object-array [nil])]
          (is (identical? v (aset a 0 v)))
          (is (identical? v (aget a 0))))))

    (testing "works on arrays of all types"
      ;; `array` is bound so that the write and the read see the same array:
      ;; `are` substitutes its argument forms, so an unbound occurrence of
      ;; `array` in both places would build two different arrays.
      (are [expected array val] (= expected
                                   (let [a array]
                                     (aset a 0 val)
                                     (aget a 0)))
        :a  (object-array 2) :a
        1   (int-array 2) 1
        2   (long-array 2) 2
        1.5 (double-array 2) 1.5)
      ;; CLJS has no array types beyond object/long/int/double, so these
      ;; dialect-specific primitive arrays cannot be built there. An array of
      ;; bytes, shorts, or chars cannot hold a plain long either, so those
      ;; values are cast to the element type first.
      #?(:cljs nil
         :default
         (are [expected array val] (= expected
                                      (let [a array]
                                        (aset a 0 val)
                                        (aget a 0)))
           (char 98) (char-array 2) (char 98)
           true      (boolean-array 2) true
           2         (byte-array 2) (byte 2)
           2         (short-array 2) (short 2)
           2.0       (float-array 2) 2.0)))

    (testing "arity-3+ treats the indices as a path into nested arrays"
      (testing "only the addressed slot changes"
        (let [a (object-array [(object-array [:a :a])
                               (object-array [:a :a])])]
          (is (= :b (aset a 1 0 :b)))
          (is (= :b (aget a 1 0)))
          (is (= [[:a :a] [:b :a]] (mapv vec a)))
          (testing "the intermediate arrays are reached, not replaced"
            (let [inner (aget a 1)]
              (aset a 1 1 :c)
              (is (identical? inner (aget a 1)))
              (is (= :b (aget a 1 0)))
              (is (= :c (aget a 1 1)))))))
      (testing "`(aset a i j v)` is `(aset (aget a i) j v)`"
        (let [a (object-array [(object-array 2) (object-array 2)])
              b (object-array [(object-array 2) (object-array 2)])]
          (is (= :v (aset a 1 0 :v)))
          (is (= :v (aset (aget b 1) 0 :v)))
          (is (= (mapv vec a) (mapv vec b)))))
      (testing "three or more indices work too"
        ;; every coordinate along each path is exercised, so a misplaced
        ;; index is caught no matter which position it is in
        (let [a (object-array [(object-array [(object-array 2) (object-array 2)])
                               (object-array [(object-array 2) (object-array 2)])])]
          (aset a 0 0 0 1)
          (aset a 0 0 1 2)
          (aset a 0 1 1 3)
          (aset a 1 0 0 4)
          (aset a 1 1 1 5)
          (is (= 1 (aget a 0 0 0)))
          (is (= 2 (aget a 0 0 1)))
          (is (= 3 (aget a 0 1 1)))
          (is (= 4 (aget a 1 0 0)))
          (is (= 5 (aget a 1 1 1)))
          (is (nil? (aget a 0 1 0))))))

    (testing "an out-of-bounds index is rejected"
      ;; CLJS arrays have no bounds checking: a write past the end extends the
      ;; array and a negative index writes nothing, rather than throwing.
      #?(:cljs
         (testing "CLJS writes past the end without complaining"
           (let [a (object-array [:a])]
             (is (= :b (aset a 1 :b)))
             (is (= [:a :b] (vec a))))
           (let [a (object-array [:a :b])]
             (is (= :c (aset a -1 :c)))
             (is (= [:a :b] (vec a)))))
         :default
         (testing "indices past the end are rejected"
           (are [array index] (p/thrown? (aset array index :x))
             (object-array []) 0
             (object-array [:a]) 1
             (object-array [:a]) 1000
             (int-array [1 2 3]) 3))
         (testing "a negative index is rejected"
           (is (p/thrown? (aset (object-array [:a]) -1 :x)))
           (is (p/thrown? (aset (int-array [1 2 3]) -1 0))))
         (testing "in a nested write too"
           (let [a (object-array [(object-array 1)])]
             (is (p/thrown? (aset a 0 1 :x)))
             (is (p/thrown? (aset a 2 0 :x)))))))

    (testing "a nil array is rejected"
      ;; Some implementations inspect the array form when they inline `aset`,
      ;; where a literal `nil` is ambiguous, so the nil arrives as a function
      ;; result instead.
      (is (p/thrown? (aset (identity nil) 0 :x))))

    (testing "a nil intermediate array is rejected"
      (is (p/thrown? (let [a (object-array [nil])]
                       (aset a 0 0 :x)))))

    (testing "only arrays are accepted"
      ;; CLJS writes a property onto whatever object it is handed, leaving the
      ;; value untouched, so it accepts all of these.
      #?(:cljs nil
         :default
         (are [x] (p/thrown? (aset (identity x) 0 :x))
           [1 2 3]           ; a vector
           '(1 2 3)          ; a list
           "abc"             ; a string
           {:a 1}            ; a map
           #{:a}             ; a set
           :a)))))           ; a keyword
