(ns clojure.core-test.cycle
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists cycle
  (deftest test-cycle

    (testing "nominal cases"
      (are [n coll expected] (= expected (take n (cycle coll)))
                             1 nil []
                             1 '() []
                             1 '(1 2 3) [1]
                             3 '(1 2 3) [1 2 3]
                             7 '(1 2 3) [1 2 3 1 2 3 1]
                             3 (range) [0 1 2]
                             7 (sorted-set 1 2 3) [1 2 3 1 2 3 1]
                             3 {:a 1 :b 2} [[:a 1] [:b 2] [:a 1]]))

    (testing "bad shape"
      (are [coll] (thrown? #?(:cljs js/Error :default Exception) (cycle coll))
                  :k
                  42
                  3.14))))
