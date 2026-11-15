(ns clojure.core-test.aget
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/aget

  ;; DOCSTRING:
  ;; clojure.core/aget
  ;; [array idx]
  ;; [array idx & idxs]
  ;; Returns the value at the index/indices. Works on Java arrays of all
  ;; types.

  (deftest test-aget
    ;; Only the outer array has to be an `object-array`: a primitive array such
    ;; as `int-array` holds primitives, so its elements cannot themselves be
    ;; arrays, whereas every slot of an object array holds any value. The inner
    ;; arrays are therefore free to be of different types. (`make-array` could
    ;; also serve as the outer array, but it is not portable: the JVM and CLJR
    ;; require a leading type argument that CLJS ignores.)
    (let [nested (object-array [(int-array [1 2 3])
                                (long-array [4 5 6])])]

      (testing "arity-2 returns the value at the index"
        (is (= :a (aget (object-array [:a :b :c]) 0)))
        (is (= :b (aget (object-array [:a :b :c]) 1)))
        (is (= :c (aget (object-array [:a :b :c]) 2)))
        (is (= 3 (aget (int-array [1 2 3]) 2)))
        (is (= "str" (aget (object-array ["str"]) 0))))

      (testing "the value is returned as-is"
        (testing "false and nil are genuine elements, not absences"
          (is (false? (aget (object-array [false]) 0)))
          (is (nil? (aget (object-array [nil]) 0))))
        (testing "the element itself is returned, not a copy"
          (let [v :a-value]
            (is (identical? v (aget (object-array [v]) 0))))
          (let [v (object-array [1 2])]
            (is (identical? v (aget (object-array [v]) 0)))))
        (testing "reading does not disturb the array"
          (let [a (object-array [1 2 3])]
            (is (= 2 (aget a 1)))
            (is (= 1 (aget a 0)))
            (is (= 2 (aget a 1)))
            (is (= [1 2 3] (vec a))))))

      (testing "works on arrays of all types"
        (are [expected array index] (= expected (aget array index))
          :a (object-array [:a]) 0
          1 (int-array [1 2 3]) 0
          2 (long-array [1 2 3]) 1
          3 (long-array [1 2 3]) 2
          1.5 (double-array [1.5 2.5]) 0
          2.5 (double-array [1.5 2.5]) 1)
        ;; CLJS has no array types beyond object/long/int/double, so these
        ;; dialect-specific primitive arrays cannot be built there.
        #?(:cljs nil
           :default
           (are [expected array index] (= expected (aget array index))
             (char 98) (char-array [(char 97) (char 98)]) 1
             true (boolean-array [false true]) 1
             2 (byte-array [1 2 3]) 1
             2 (short-array [1 2 3]) 1
             2.0 (float-array [1.0 2.0 3.0]) 1))
        (testing "an unfilled slot of an object array is nil"
          (is (nil? (aget (object-array 3) 0)))
          (is (nil? (aget (object-array 3) 2)))))

      (testing "arity-3+ treats the indices as a path into nested arrays"
        (is (= 1 (aget nested 0 0)))
        (is (= 3 (aget nested 0 2)))
        (is (= 4 (aget nested 1 0)))
        (is (= 6 (aget nested 1 2)))
        (testing "`(aget a i j)` is `(aget (aget a i) j)`"
          (doseq [i [0 1]
                  j [0 1 2]]
            (is (= (aget (aget nested i) j) (aget nested i j)))))
        (testing "three or more indices work too"
          ;; every index is exercised at each level, so a misplaced index is
          ;; caught no matter which position it is in
          (let [deeper (object-array [(object-array [(int-array [1 2])
                                                     (long-array [3 4])])
                                      (object-array [(int-array [5 6])
                                                     (long-array [7 8])])])]
            (is (= 1 (aget deeper 0 0 0)))
            (is (= 2 (aget deeper 0 0 1)))
            (is (= 3 (aget deeper 0 1 0)))
            (is (= 4 (aget deeper 0 1 1)))
            (is (= 5 (aget deeper 1 0 0)))
            (is (= 6 (aget deeper 1 0 1)))
            (is (= 7 (aget deeper 1 1 0)))
            (is (= 8 (aget deeper 1 1 1)))
            (testing "`(aget a i j k)` is `(aget (aget (aget a i) j) k)`"
              (doseq [i [0 1]
                      j [0 1]
                      k [0 1]]
                (is (= (aget (aget (aget deeper i) j) k)
                       (aget deeper i j k)))))))
        (testing "the nested arrays need not be rectangular"
          (let [ragged (object-array [(int-array [1 2 3])
                                      (long-array [4])])]
            (is (= 3 (aget ragged 0 2)))
            (is (= 4 (aget ragged 1 0)))))
        (testing "the indices are followed in order"
          ;; every pair below differs from its mirror image
          (is (= 4 (aget nested 1 0)))
          (is (= 2 (aget nested 0 1)))))

      (testing "an out-of-bounds index is rejected"
        ;; CLJS arrays have no bounds checking: past-the-end reads yield an
        ;; empty slot rather than throwing.
        #?(:cljs
           (testing "CLJS yields an empty slot"
             (is (nil? (aget (object-array [:a]) 1)))
             (is (nil? (aget (object-array []) 0)))
             (is (nil? (aget nested 0 3))))
           :default
           (testing "indices past the end are rejected"
             (are [array index] (p/thrown? (aget array index))
               (object-array []) 0
               (object-array [:a]) 1
               (int-array []) 0
               (int-array [1 2 3]) 3
               (int-array [1 2 3]) 1000)
             (testing "in a nested access too"
               (is (p/thrown? (aget nested 2 0)))
               (is (p/thrown? (aget nested 0 3)))))))

      (testing "a negative index is rejected"
        #?(:cljs
           (testing "CLJS yields an empty slot"
             (is (nil? (aget (object-array [:a]) -1)))
             (is (nil? (aget nested 0 -1))))
           :default
           (are [array index] (p/thrown? (aget array index))
             (object-array [:a]) -1
             (int-array [1 2 3]) -1)))

      (testing "a nil array is rejected"
        ;; Some implementations inspect the array form when they inline
        ;; `aget`, where a literal `nil` is ambiguous, so the nil arrives as a
        ;; function result instead.
        (is (p/thrown? (aget (identity nil) 0))))

      (testing "only arrays are accepted"
        ;; CLJS indexes any object, so it accepts all of these.
        #?(:cljs nil
           :default
           (are [x] (p/thrown? (aget (identity x) 0))
             [1 2 3]           ; a vector
             '(1 2 3)          ; a list
             "abc"             ; a string
             {:a 1}            ; a map
             #{:a}             ; a set
             :a))))))          ; a keyword
