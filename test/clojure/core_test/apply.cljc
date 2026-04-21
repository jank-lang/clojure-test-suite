(ns clojure.core-test.apply
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists apply
  (deftest test-apply
    (is (= 0 (apply + nil)))            ; Apply + with no args
    (is (= 0 (apply + '())))
    (is (= 0 (apply + [])))
    (is (= 0 (apply + {})))             ; map is a sequence of map entries
    (is (= 0 (apply + #{})))
    (is (= 0 (apply + "")))             ; string is a sequence of characters

    (is (= 1 (apply + 1 nil)))          ; Apply + with 1 arg and empty sequence
    (is (= 1 (apply + 1 '())))
    (is (= 1 (apply + 1 [])))
    (is (= 1 (apply + 1 {})))
    (is (= 1 (apply + 1 #{})))
    (is (= 1 (apply + 1 "")))

    (is (= 1 (apply + '(1))))           ; Zero non-sequence arg
    (is (= 3 (apply + 1 '(2))))         ; One non-sequential arg
    (is (= 6 (apply + 1 2 '(3))))       ; Two non-sequential args
    (is (= 10 (apply + 1 2 3 '(4))))    ; Three non-sequential args
    (is (= 15 (apply + 1 2 3 4 '(5))))  ; Four non-sequential args
    (is (= 45 (apply + (range 10))))    ; All args as sequential
    (is (= 10 (apply + 1 [2 3 4])))
    (is (= 10 (apply + 1 #{2 3 4})))    ; A set works but order not guaranteed
    (is (= #{[:a 1] [:b 2]}             ; Can't guarantee order of map entries
           (apply conj #{} {:a 1, :b 2})))
    (is (= [\a \b \c]                   ; String is sequence of chars
           (apply conj [] "abc")))

    ;; Try various IFn things
    (is (= 1 (apply {:a 1} [:a])))            ; apply map to key
    (is (= 1 (apply :a [{:a 1}])))            ; apply keyword to map
    (is (= 2 (apply [0 1 2 3 4] [2])))        ; apply vector to index
    (is (p/thrown? (apply 2 [[0 1 2 3 4]])))  ; but numbers don't implement IFn
    (is (= :a (apply #{:a :b :c} [:a])))      ; apply set to element
    (is (= :a (apply :a [#{:a :b :c}])))      ; apply keyword to set

    ;; apply recrusively
    (is (= 10 (apply apply + [1 2 [3 4]])))

    ;; validate that `apply` doesn't try to further evaluate its
    ;; arguments. If the infinite range is realized, we would expect
    ;; an OOM Exception at some point.
    (is (= 3 (count (apply conj [] [1 2 (range)]))))))
