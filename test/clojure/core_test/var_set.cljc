(ns clojure.core-test.var-set
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists var-set
  (deftest test-var-set
    (testing "set value in with-local-vars"
      (with-local-vars [x 1]
        (is (= 1 @x))
        (var-set x 10)
        (is (= 10 @x))))

    (testing "set to different types"
      (with-local-vars [x 1]
        (var-set x "string")
        (is (= "string" @x))
        (var-set x [1 2 3])
        (is (= [1 2 3] @x))
        (var-set x {:a 1})
        (is (= {:a 1} @x))
        (var-set x :keyword)
        (is (= :keyword @x))))

    (testing "set to nil"
      (with-local-vars [x 42]
        (var-set x nil)
        (is (nil? @x))))

    (testing "set multiple times"
      (with-local-vars [x 1]
        (var-set x 2)
        (is (= 2 @x))
        (var-set x 3)
        (is (= 3 @x))
        (var-set x 4)
        (is (= 4 @x))))

    (testing "set in nested scopes"
      (with-local-vars [x 1]
        (var-set x 10)
        (is (= 10 @x))
        (let [y 20]
          (var-set x y)
          (is (= 20 @x)))))

    (testing "set with function result"
      (with-local-vars [x 5]
        (var-set x (* @x 2))
        (is (= 10 @x))
        (var-set x (inc @x))
        (is (= 11 @x))))

    (testing "set in loop"
      (with-local-vars [acc 1 cnt 5]
        (while (> @cnt 0)
          (var-set acc (* @acc @cnt))
          (var-set cnt (dec @cnt)))
        (is (= 120 @acc))
        (is (= 0 @cnt))))

    (testing "set to collection values"
      (with-local-vars [x []]
        (var-set x (conj @x 1))
        (is (= [1] @x))
        (var-set x (conj @x 2))
        (is (= [1 2] @x))))

    (testing "multiple vars"
      (with-local-vars [a 1 b 2 c 3]
        (var-set a 10)
        (var-set b 20)
        (var-set c 30)
        (is (= 10 @a))
        (is (= 20 @b))
        (is (= 30 @c))))

    (testing "set returns the new value"
      (with-local-vars [x 1]
        (is (= 42 (var-set x 42)))
        (is (= "test" (var-set x "test")))
        (is (nil? (var-set x nil)))))

    (testing "set with numeric operations"
      (with-local-vars [sum 0]
        (dotimes [i 10]
          (var-set sum (+ @sum i)))
        (is (= 45 @sum))))

    (testing "set boolean values"
      (with-local-vars [flag true]
        (is (= true @flag))
        (var-set flag false)
        (is (= false @flag))
        (var-set flag (not @flag))
        (is (= true @flag))))

    (testing "set to function"
      (with-local-vars [f +]
        (is (= 3 (@f 1 2)))
        (var-set f *)
        (is (= 6 (@f 2 3)))))

    (testing "set with larger data structures"
      (with-local-vars [data {}]
        (var-set data (assoc @data :a 1))
        (var-set data (assoc @data :b 2))
        (is (= {:a 1 :b 2} @data))))))
