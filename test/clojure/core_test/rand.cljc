(ns clojure.core-test.rand
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability :as p]))

(p/when-var-exists rand
  (deftest test-rand
    ;; Generally, we test that the numbers returned pass `double?` and
    ;; that they are not constant.
    (let [length 100]
      (testing "zero-arg case"
        (let [x (repeatedly length rand)]
          (is (every? double? x))
          (is (every? pos? x))
          (is (> (count (set x)) 1))))  ; shouldn't be constant
      (testing "one-arg case"
        (let [limit 0.01 ; Choose something < 1 to constrain it further
              x (repeatedly length #(rand limit))]
          (is (every? double? x))
          (is (every? pos? x))
          (is (> (count (set x)) 1))    ; shouldn't be constant
          (is (every? #(< % limit) x)))))))
