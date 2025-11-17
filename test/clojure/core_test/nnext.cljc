(ns clojure.core-test.nnext
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists nnext
  (deftest test-nnext
    (testing "common"
      (is (= '(2 3 4 5 6 7 8 9) (nnext (range 0 10))))
      (is (= 2 (first (nnext (range)))))
      (is (= '(3) (nnext [1 2 3])))
      (is (= '([:c 3] [:d 4]) (nnext {:a 1, :b 2, :c 3, :d 4})))
      (is (nil? (nnext nil)))
      (is (nil? (nnext '())))
      (is (nil? (nnext [])))
      (is (nil? (nnext #{}))))))
