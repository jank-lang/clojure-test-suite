(ns clojure.core-test.contains-qmark
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists contains?

  ;; Docstring:
  ;; Returns true if key is present in the given collection, otherwise
  ;; returns false.  Note that for numerically indexed collections like
  ;; vectors and Java arrays, this tests if the numeric key is within the
  ;; range of indexes. 'contains?' operates constant or logarithmic time;
  ;; it will not perform a linear search for a value.  See also 'some'.

  (deftest test-contains?
    (are [expected coll key] (= expected (contains? coll key))
      ;; maps
      true {:a 1} :a
      true {:a 1 :b 2} :b
      false {:a 1 :b 2} :c
      false {:a 1 :b 2} 1               ; only applies to keys, not vals
      true {nil :a} nil                 ; doesn't matter if key is nil
      true {:a nil} :a                  ; doesn't matter if val is nil
      true {[:a 1] 17} [:a 1]           ; keys can be collections
      true {^:foo [:a 1] 17} [:a 1]     ; metadata doesn't matter
      true {^:foo [:a 1] 17} ^:bar [:a 1]
      true {[:a 1] 17} ^:bar [:a 1]
      false {[:a 1] 17} [:a 2]
      true (sorted-map :a 1 :b 2) :a    ; no difference if sorted maps
      false (sorted-map :a 1 :b 2) :c
      true (sorted-map nil 1) nil
      false {} :a
      false {} nil
      false nil :a
      false nil nil

      ;; sets
      true #{:a} :a
      true #{:a :b} :b
      false #{:a :b} :c
      true #{:a nil} nil
      true #{nil :a} :a
      true #{[:a 1] [:b 2]} [:a 1]
      true #{[:a 1] [:b 2]} [:b 2]
      true #{^:foo [:a 1] [:b 2]} [:a 1]  ; metadata doesn't matter
      true #{^:foo [:a 1] [:b 2]} ^:bar [:a 1]
      true #{[:a 1] [:b 2]} ^:bar [:a 1]
      false #{[:a 1] [:b 2]} [:a 2]
      true (sorted-set :a :b) :a
      false (sorted-set :a :b) :c
      true (sorted-set :a nil :b) nil
      false #{} :a
      false #{} nil

      ;; vectors
      true [0 1 2] 0                    ; has indexes 0..2
      true [0 1 2] 1                    ; has indexes 0..2
      true [0 1 2] 2                    ; has indexes 0..2
      false [0 1 2] 3
      false [4 5 6] 4                   ; looks for index 4, not value 4
      false [0 1 2] -1
      false [0 1 2] nil                 ; false if not numeric index
      false [0 1 2] :a
      false [0 1 2] [0 1 2]             ; vectors aren't associative
      false [] 0
      false [] 1
      false [] -1

      ;; arrays
      true (int-array [0 1 2]) 0                    ; has indexes 0..2
      true (int-array [0 1 2]) 1                    ; has indexes 0..2
      true (int-array [0 1 2]) 2                    ; has indexes 0..2
      false (int-array [0 1 2]) 3
      false (int-array [4 5 6]) 4                   ; looks for index 4, not value 4
      false (int-array [0 1 2]) -1
      false (int-array []) 0
      false (int-array []) 1
      false (int-array []) -1

      ;; map entries
      true (first {:a 1}) 0             ; map entries contain two items
      true (first {:a 1}) 1
      false (first {:a 1}) 2

      ;; strings
      true "abc" 0
      true "abc" 1
      true "abc" 2
      false "abc" 3
      false "abc" -1
      false "" 0
      false "" 1
      false "" -1)

    ;; Does a map contain a map-entry
    (let [m1 {:a 1}
          m2 {(first m1) true}]         ; put a map-entry in m2 as key
      (is (false? (contains? m1 (first m1)))) ; m1 doesn't contain map-entry as key
      (is (contains? m2 (first m1))))

    #?@(
        ;; CLJS just returns false for any args it doesn't like, but
        ;; doesn't throw
        :cljs
        [(is (false? (contains? (int-array [0 1 2]) nil)))
         (is (false? (contains? (int-array [0 1 2]) :a)))
         (is (false? (contains? (int-array [0 1 2]) [0 1 2])))
         (is (false? (contains? "abc" \c)))
         (is (false? (contains? "abc" "c")))
         (is (false? (contains? '(1 2 3) 0)))
         (is (false? (contains? '(1 2 3) 3)))
         (is (false? (contains? 42 0)))
         (is (false? (contains? :a :a)))]

        :default
        [(is (p/thrown? (contains? (int-array [0 1 2]) nil)))
         (is (p/thrown? (contains? (int-array [0 1 2]) :a)))
         (is (p/thrown? (contains? (int-array [0 1 2]) [0 1 2])))
         (is (p/thrown? (contains? "abc" \c)))
         (is (p/thrown? (contains? "abc" "c")))
         ;; While you can get an element in a list with `nth`, it's not an
         ;; indexed collection, so it isn't supported with `contains?`.
         (is (p/thrown? (contains? '(1 2 3) 0)))
         (is (p/thrown? (contains? '(1 2 3) 3)))
         (is (p/thrown? (contains? 42 0)))
         (is (p/thrown? (contains? :a :a)))])))
