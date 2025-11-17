(ns clojure.core-test.rest
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists rest
  (deftest test-rest
    (is (= '(1 2 3 4 5 6 7 8 9) (rest (range 0 10))))
    (is (= '(2 3 4 5 6 7 8 9) (rest (rest (range 0 10)))))
    (is (= 1 (first (rest (range)))))
    (is (= '(2 3) (rest [1 2 3])))
    (is (= '() (rest nil)))
    (is (= '() (rest '())))
    (is (= '() (rest [])))))
