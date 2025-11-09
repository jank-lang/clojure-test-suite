(ns clojure.core-test.subvec
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists subvec
  (deftest test-subvec

    (testing "subvec vec start"
      (are [expected vec start] (= expected (subvec vec start))
                                [2 3 4] [0 1 2 3 4] 2
                                [1 2 3 4] [0 1 2 3 4] 1
                                [] [1 2 3 4 5] 5
                                [] [] 0))

    (testing "subvec vec start end"
      (are [expected vec start end] (= expected (subvec vec start end))
                                    [2 3] [0 1 2 3 4] 2 4
                                    [1 2 3 4] [0 1 2 3 4] 1 5
                                    [] [1 2 3 4 5] 2 2
                                    [] [] 0 0))

    (testing "borderline indices"
      (testing "NaN and floats"
        (are [expected vec start end] (= expected (subvec vec start end))
                                      [] [0 1 2] ##NaN ##NaN
                                      [0 1 2] [0 1 2] ##NaN 3
                                      [] [0 1 2] 0 ##NaN
                                      [0 1 2] [0 1 2] -0 3
                                      [2] [0 1 2] 2.72 3.14))
      (testing "ratios"
        #?(:cljs    "cljs doesn't have ratio"
           :default (is (= [0] (subvec [0 1 2] 1/2 4/3)))))
      (testing "cljs-only casts to number"
        #?(:cljs
           (are [expected vec start end] (= expected (subvec vec start end))
                [0 1] [0 1 2] :a 2
                [] [0 1 2] 0 :b
                [] [0 1 2] 'c 'd
                [0 1 2] [0 1 2] "a" 3
                [] [0 1 2] [] {}
                [0] [0 1 2] false true
                [0 1 2 3] [0 1 2 3] ##-Inf 4
                [] [0 1 2 3] 0 ##Inf))))

    (testing "out-of-bounds"
      (are [vec start end] (thrown? #?(:cljs js/Error :default Exception) (subvec vec start end))
                           [0 1 2 3] -1 3
                           [0 1 2 3] 1 5
                           [0 1 2 3] 3 2
                           [] 0 1))

    (testing "bad shapes"
      (testing "nil args"
        (are [vec start end] (thrown? #?(:cljs js/Error :default Exception) (subvec vec start end))
                             nil 0 0
                             [] nil 0
                             [0 1 2] 1 nil))
      (testing "not a vector"
        (are [vec start end] (thrown? #?(:cljs js/Error :default Exception) (subvec vec start end))
                             '(0 1 2) 0 2
                             #{0 1 2} 0 2
                             {:a 0 :b 1} 0 2
                             (range 3) 0 2
                             "012" 0 2
                             (transient [0 1 2]) 0 2))
      (testing "indices that cannot be cast to numbers"
        #?(:cljs    "cljs can actually cast these to numbers"
           :default (are [vec start end] (thrown? Exception (subvec vec start end))
                                         [0 1 2] :a 2
                                         [0 1 2] 1 :b
                                         [0 1 2] 'c 'd
                                         [0 1 2] "a" "b"
                                         [0 1 2] [] {}
                                         [0 1 2 3] ##-Inf 4
                                         [0 1 2 3] 0 ##Inf))))))
