(ns clojure.core-test.counted-qmark
  (:require [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists counted?
  (deftest test-counted?
    (testing "positive tests"
      (are [x] (counted? x)
        [1 2 3]
        '(1 2 3)                        ; surprising for traditional Lispers
        #{1 2 3}
        #?@(:cljs [nil] :default [])    ; CLJS nil is `counted?`
        (array-map :a 1 :b 2 :c 3)
        (hash-map :a 1 :b 2 :c 3)
        (sorted-map :a 1 :b 2 :c 3)
        (sorted-set 1 2 3)))
    (testing "negative tests"
      (are [x] (not (counted? x))
        1
        1N
        1.0
        1.0M
        :a-keyword
        'a-sym
        #?@(:cljs [] :default [nil])    ; surprising since `(count nil)` = 0
        ;; `count` works on strings, arrays, and other Java
        ;; collections, but they are not `counted?`.
        "a string"
        (object-array 3)))))
