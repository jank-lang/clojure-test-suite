(ns clojure.core-test.sequential-qmark
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists sequential?
  (deftest test-sequential?
    (are [expected x] (= expected (sequential? x))
      ;; lists, vectors, and seqs are sequential
      true '()
      true '(1 2 3)
      true []
      true [1 2 3]
      true (range 10)
      true (range)                      ; Infinite lazy seq
      true (seq "abc")                  ; seqs are sequential even if strings aren't
      true (seq (to-array [1 2 3]))     ; arrays aren't

      ;; other things are not sequential
      false nil                         ; perhaps surprising
      false {:a 1 :b 2}
      false #{:a :b}
      false "abc"
      false (to-array [1 2 3])
      false :a
      false 'a
      false 1
      false \a)))
