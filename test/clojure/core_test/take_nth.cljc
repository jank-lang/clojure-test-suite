(ns clojure.core-test.take-nth
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists take-nth
  (deftest test-take-nth
    (testing "Basic arity-2"
      (are [expected n coll] (= expected (take-nth n coll))
        (range 0 10 1) 1 (range 10)
        (range 0 10 2) 2 (range 10)
        (range 0 10 3) 3 (range 10)
        '(\C \o \u \e \R \c \s) 2 "Clojure Rocks" ; works on any seq
        () 2 nil))

    ;; 1-arity transducer
    (testing "Arity-1 transducer"
      (are [expected n coll] (= (vec expected) (transduce (take-nth n) conj [] coll))
        (range 0 10 1) 1 (range 10)
        (range 0 10 2) 2 (range 10)
        (range 0 10 3) 3 (range 10)
        '(\C \o \u \e \R \c \s) 2 "Clojure Rocks" ; works on any seq
        () 2 nil))

    (testing "Negative cases"
      ;; Note: passing a non-positive integer (either zero or
      ;; negative) to the arity-2 version take-nth results in an
      ;; infinite loop. Do not do this:
      #_(take-nth 0 (range 10))         ; infinite loop

      ;; But you can pass negative numbers, but not zero, to the
      ;; arity-1 version and it treats it the same as if n was
      ;; positive. If you pass in zero, it throws an exception (see
      ;; below).
      (are [expected n coll] (= (vec expected) (transduce (take-nth n) conj [] coll))
        (range 0 10 1) -1 (range 10)
        (range 0 10 2) -2 (range 10))

      (is (thrown? #?(:cljs :default :default Exception)
                   (seq (take-nth nil (range 10)))))
      (is (thrown? #?(:cljs :default :default Exception)
                   (transduce (take-nth nil) conj [] (range 10))))
      #?(:cljs
         (is (= [] (transduce (take-nth 0) conj [] (range 10))))
         :default
         (is (thrown? Exception (transduce (take-nth 0) conj [] (range 10))))))))
