(ns clojure.core-test.some
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists some
  (deftest test-some

    (testing "lists"
      (is (= true (some even? '(1 2 3 4))))
      (is (= nil (some even? '(1 3 5 7))))
      (is (= nil (some even? '()))))

    (testing "sets"
      (is (= true (some odd? #{1 2 3 4})))
      (is (= nil (some odd? #{2 4 6 8})))
      (is (= nil (some odd? #{}))))

    (testing "vectors"
      (is (= nil (some true? [false false false])))
      (is (= true (some true? [false true false])))
      (is (= true (some true? [true true true])))
      (is (= nil (some true? []))))

    (testing "other seqables"
      (is (= true (some char? "string is seqable")))
      (is (= nil (some char? "")))
      (is (= true (some even? (int-array [1 2 3 4]))))
      (is (= true (some even? (long-array [1 2 3 4]))))
      (is (= true (some pos? (double-array [-1.0 1.0]))))
      #?(:cljs () :default (is (= true (some pos? (float-array [-1.0 1.0]))))))

    (testing "custom predicates"
      (is (= true (some #(= 5 %) [1 2 3 4 5])))
      (is (= nil (some #(= 5 %) [6 7 8 9 10])))
      (is (= true (some #(not= 5 %) [1 2 3 4 5])))
      (is (= true (some #(not= 5 %) [6 7 8 9 10]))))

    (testing "set predicates"
      (is (= 2 (some #{2} (range 0 10))))
      (is (= 2 (some #{6 2 4} (range 0 10))))
      (is (= 4 (some #{2 4 6} (range 3 10))))
      (is (= nil (some #{200} (range 0 10)))))

    (testing "map predicates"
      (is (= "three" (some {2 "two" 3 "three"} [nil 3 2])))
      (is (= "nothing" (some {nil "nothing" 2 "two" 3 "three"} [nil 3 2])))
      (is (= "two" (some {2 "two" 3 nil} [nil 3 2]))))

    (testing "nils"
      (is (= nil (some #{nil} [1 nil 2 false 3])))
      (is (= true (some nil? [1 nil 2 false 3])))
      (is (= true (some false? [1 nil 2 false 3])))
      (is (= true (some not [1 nil 2 false 3])))
      (is (= nil (some nil? nil))))

    (testing "coll as predicates"
      (is (= nil (some #(% 3 7) (list #(= %1 %2)))))
      (is (= true (some #(% 3 3) (list #(= %1 %2))))))

    (testing "apply or"
      (is (= 5 (some identity [nil false 5 nil 7]))))

    (testing "bad shape"
      (are [coll] (thrown? #?(:cljs js/Error :default Exception) (some identity coll))
                  :some
                  42
                  3.14))))
