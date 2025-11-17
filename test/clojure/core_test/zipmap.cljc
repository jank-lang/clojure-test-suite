(ns clojure.core-test.zipmap
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists zipmap
 (deftest test-zipmap
   (testing "Common cases"
     (are [in ex] (= (apply zipmap in) ex)
       [[1 2 3]  ["a" "b" "c"]]   {1 "a" 2 "b" 3 "c"}
       [#{1}     #{"a"}]          {1 "a"}
       [(sorted-set 1 2 3)
        (sorted-set "a" "b" "c")] {1 "a" 2 "b" 3 "c"}
       [(sorted-map :a 1 :b 2)
        ["a" "b"]]                {[:a 1] "a" [:b 2] "b"}
       ['(1 2 3) '("a" "b" "c")]  {1 "a" 2 "b" 3 "c"}
       ["123"    "abc"]           {\1 \a \2 \b \3 \c}
       [[:a]     [nil]]           {:a nil}
       [[nil]    [:a]]            {nil :a}))
   (testing "Differing sequence sizes"
     (are [in ex] (= (apply zipmap in) ex)
       [[1 2]    ["a" "b" "c"]] {1 "a" 2 "b"}
       ['(1 2 3) '("a" "b")]    {1 "a" 2 "b"}
       [(range)  '("a" "b")]    {0 "a" 1 "b"}
       [(range)  nil]           {}
       [nil      (range)]       {}))
   (testing "Esoteric cases"
     (are [in ex] (= (apply zipmap in) ex)
       [(range)  '("a" "b")]    {0 "a" 1 "b"}))
   (testing "Bad inputs"
     (is (thrown? #?(:cljs :default :default Exception) (zipmap :not-seqable [1 2 3])))
     (is (thrown? #?(:cljs :default :default Exception) (zipmap 123          [1 2 3]))))))
