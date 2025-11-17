(ns clojure.core-test.cons
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists cons
  (deftest test-cons

    (testing "finite seqs"
      (are [x seq expected] (= expected (cons x seq))
                            1 [2 3] [1 2 3]
                            1 '(2 3) [1 2 3]
                            \1 "23" [\1 \2 \3]
                            1 (sorted-set 1 2 3) [1 1 2 3]
                            1 {:2 2 :3 3} [1 [:2 2] [:3 3]]
                            [0 1] '(2 3) [[0 1] 2 3]))

    (testing "infinite seqs"
      (is (= -1 (first (cons -1 (range))))))

    (testing "nil and empty"
      (are [x seq expected] (= expected (cons x seq))
                            nil nil [nil]
                            1 nil [1]
                            1 "" [1]
                            1 '() [1]
                            1 #{} [1]
                            1 {} [1]
                            1 [] [1]))

    (testing "bad shape"
      (are [seq] (thrown? #?(:cljs js/Error :default Exception) (cons 1 seq))
                 :k
                 42
                 3.14
                 true
                 false
                 cons))))
