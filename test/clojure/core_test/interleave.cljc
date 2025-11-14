(ns clojure.core-test.interleave
  (:require [clojure.test :as t :refer [are deftest testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists
 interleave
 (deftest test-interleave
   (testing "common cases"
     (are [in ex] (= (apply interleave in) ex)
       [[1 2 3]]                  [1 2 3]
       [[1 2 3] ["a" "b" "c"]]    [1 "a" 2 "b" 3 "c"]
       [[1 2 3]
        ["a" "b" "c"]
        [\a \b \c]]               [1 "a" \a 2 "b" \b 3 "c" \c]
       [#{1}     #{"a"}]          [1 "a"]
       [(sorted-set 1 2 3)
        (sorted-set "a" "b" "c")] [1 "a" 2 "b" 3 "c"]
       [(sorted-map :a 1 :b 2)
        ["a" "b"]]                [[:a 1] "a" [:b 2] "b"]
       ['(1 2 3) '("a" "b" "c")]  [1 "a" 2 "b" 3 "c"]
       ["123"    "abc"]           [\1 \a \2 \b \3 \c]
       [[:a]     [nil]]           [:a nil]
       [[nil]    [:a] [nil]]      [nil :a nil])
     (testing "Differing sequence sizes, nil inputs"
       (are [in ex] (= (apply interleave in) ex)
         [[1 2]
          ["a" "b" "c"]
          "1234567"]              [1 "a" \1 2 "b" \2]
         [[1 2 3 4 5]
          ["a" "b" "c"]
          "12"]                   [1 "a" \1 2 "b" \2]
         ['(1 2 3) '("a" "b")]    [1 "a" 2 "b"]
         [(range)  '("a" "b")]    [0 "a" 1 "b"]
         [(range)  nil]           []
         [nil      (range)]       [])))))
