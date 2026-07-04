(ns clojure.core-test.take-last
  (:require [clojure.test :as t :refer [deftest is are testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists take-last
  (deftest test-take-last
    ;; Notes:
    ;; 1. There is no transducer version of `take-last` in
    ;;    `clojure.core`.
    ;; 2. `take-last` does not return a lazy seq.
    (testing "basic cases"
      ;; take-last of various lengths
      (is (nil? (take-last 0 (range 10))))             ; take-last 0
      (is (= '(9) (take-last 1 (range 10))))           ; take-last 1
      (is (= (range 8 10) (take-last 2 (range 0 10)))) ; take-last 2
      (is (= '(0 1 2 3 4) (take 5 (range 5))))  ; take = length of seq
      (is (= '(0 1 2 3 4) (take 10 (range 5)))) ; take > length of seq

      ;; take-last with empty collections
      (is (nil? (take-last 2 nil)))     ; Returns `nil`, not `()`
      (is (nil? (take-last 2 '())))
      (is (nil? (take-last 2 []))))

    (testing "negative cases"
      (is (p/thrown? (doall (take-last nil (range 0 10))))))))
