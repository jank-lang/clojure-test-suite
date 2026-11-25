(ns clojure.core-test.alength
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/alength

  ;; DOCSTRING:
  ;; clojure.core/alength
  ;; [array]
  ;; Returns the length of the Java array. Works on arrays of all
  ;; types.

  (deftest test-alength
    (testing "returns the number of elements in the array"
      (are [expected array] (= expected (alength array))
        0 (object-array [])
        1 (object-array [:a])
        3 (object-array [:a :b :c])
        2 (int-array [10 20])
        4 (long-array [1 2 3 4])
        1000 (object-array 1000)))

    (testing "counts slots, not the elements stored in them"
      (is (= 3 (alength (object-array 3))))
      (is (= 3 (alength (object-array [nil nil nil]))))
      (is (= 3 (alength (object-array [1 nil 3])))))

    (testing "works on arrays of all types"
      (are [expected array] (= expected (alength array))
        3 (object-array 3)
        3 (int-array 3)
        3 (long-array 3)
        3 (double-array 3)
        0 (object-array 0)
        0 (int-array 0)
        0 (double-array 0))
      ;; CLJS has no array types beyond object/long/int/double, so these
      ;; dialect-specific primitive arrays cannot be built there.
      #?(:cljs nil
         :default
         (are [expected array] (= expected (alength array))
           3 (boolean-array 3)
           3 (byte-array 3)
           3 (short-array 3)
           3 (float-array 3)
           3 (char-array 3)
           0 (char-array 0))))

    (testing "counts the array's own slots, not those of its contents"
      (is (= 2 (alength (object-array [(int-array [1 2 3])
                                       (int-array [4 5])]))))
      (is (= 1 (alength (object-array [(object-array 5)]))))
      (is (= 1 (alength (object-array ["abc"])))))

    (testing "a nil array is rejected"
      ;; Some implementations inspect the array form when they inline
      ;; `alength`, where a literal `nil` is ambiguous, so the nil arrives as a
      ;; function result instead.
      (is (p/thrown? (alength (identity nil)))))

    (testing "only arrays are accepted"
      ;; CLJS reads a `length` property off whatever object it is handed, so it
      ;; accepts all of these.
      #?(:cljs nil
         :default
         (are [x] (p/thrown? (alength (identity x)))
           [1 2 3]           ; a vector
           '(1 2 3)          ; a list
           "abc"             ; a string
           {:a 1}            ; a map
           #{:a}             ; a set
           :a                ; a keyword
           1)))))            ; a number
