(ns clojure.core-test.peek
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists peek
  (deftest test-peek

    (testing "list"
      (is (nil? (peek '())))
      (is (= :a (peek '(:a :b :c)))))

    (testing "vector"
      (is (nil? (peek [])))
      (is (= :c (peek [:a :b :c]))))

    (testing "nil"
      (is (nil? (peek nil))))

    (testing "bad shape"
      ;; Phel only throws for sets and non-seqable scalars. It is lenient on
      ;; maps (=> nil), lists/cons and lazy seqs (peeks the head), and strings
      ;; (peeks the last char, since strings are vector-like here).
      #?(:phel
         (do
           (is (p/thrown? (peek #{1 2 3})))
           (is (nil? (peek {:a 1 :b 2})))
           (is (= 1 (peek (cons 1 '()))))
           (is (= 0 (peek (range 10))))
           (is (= "r" (peek "str")))
           (is (p/thrown? (peek 42))))
         :default
         (are [coll] (p/thrown? (peek coll))
                     #{1 2 3}
                     {:a 1 :b 2}
                     (cons 1 '())
                     (range 10)
                     "str"
                     42)))))
