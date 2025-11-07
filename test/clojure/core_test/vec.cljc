(ns clojure.core-test.vec
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vec
  (deftest test-vec

    (testing "vec coll"
      (are [expected coll] (= expected (vec coll))
                           [] nil
                           [] '()
                           [] []
                           [] #{}
                           [] {}
                           [] ""
                           [nil nil nil] '(nil nil nil)
                           [1 2 3] '(1 2 3)
                           [1 2 3] [1 2 3]
                           [1 2 3] (sorted-set 1 2 3)
                           [1 2 3] (range 1 4)
                           [[:a 1] [:b 2]] {:a 1 :b 2}
                           [\a \b \c] "abc"))

    #?(:cljr    "cljr does not alias array"
       :default (testing "array aliasing"
                  (let [arr (to-array [1 2 3]), v (vec arr)]
                    (is (= [1 2 3] v))
                    (aset arr 0 -1)
                    (is (= [-1 2 3] v)))))

    (testing "bad shape"
      (are [arg] (thrown? #?(:cljs js/Error :default Exception) (vec arg))
                 42
                 3.14
                 true
                 :a
                 (transient [])))))
