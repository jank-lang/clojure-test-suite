(ns clojure.core-test.aclone
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists aclone
  (defn clone-test
    [a b]
    (aset a 0 1)
    (aset a 1 2)
    (aset a 2 3)
    (let [a' (aclone a)
          b' (aclone b)]
      (is (= 3 (alength a')))
      (is (every? identity (map #(= (aget a %) (aget a' %)) (range 3))))
      (is (zero? (alength b')))
      (is (not (identical? a a')))
      (is (not (identical? b b')))
      (aset a 1 11)
      (is (= 11 (aget a 1)))
      (is (= 2 (aget a' 1)))
      (aset a' 2 12)
      (is (= 3 (aget a 2)))
      (is (= 12 (aget a' 2)))))

  (deftest test-aclone
    (testing "integer-arrays"
      (clone-test (int-array 3) (int-array 0)))

    (testing "object-arrays"
      (clone-test (object-array 3) (object-array 0)))))
