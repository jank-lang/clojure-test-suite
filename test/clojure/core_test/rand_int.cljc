(ns clojure.core-test.rand-int
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability :as p]))

(p/when-var-exists rand-int
  (deftest test-rand-int
    ;; Generally, we test that the numbers returned pass `int?` and
    ;; that they are not constant.
    (let [length 100
          limit 2000000000
          x (repeatedly length #(rand-int limit))]
      (is (every? int? x))
      (is (every? pos? x))
      (is (> (count (set x)) 1))        ; Shouldn't be constant
      (is (every? #(< % limit) x)))))
