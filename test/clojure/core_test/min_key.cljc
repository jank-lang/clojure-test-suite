(ns clojure.core-test.min-key
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))


(when-var-exists min-key
  (defn my-abs [x]
    (if (< x 0)
      (- x)
      x))

  (defn my-sqr [x]
    (* x x))

  ;; to help with nan testing
  (defn =-or-NaN? [x y]
    (or (= x y)
        (and (NaN? x) (NaN? y))))

  (deftest test-min-key
    ;; adapted from `min.cljc`
    (testing "numeric ordering"
      (are [expected f col] (= expected (apply min-key f col))
        1 identity [1 2]
        1 identity [1 3 2]
        1N identity [1N 2N]
        1N identity [2N 1N 3N]
        1N identity [1N 2]
        1 identity [1 2N]
        1.0 identity [1.0 2.0]
        1 identity [1 2.0]
        1.0 identity [1.0 2]
        #?@(:cljs []
            :default
            [1/2 identity [1/2 2/2]
             1/2 identity [1/2 2/2 3/2]
             1/2 identity [1/2 1]
             1/2 identity [1/2 1 2N]])))

    ;; NaN ordering weirdness because `(<= ##NaN 1)` and `(<= 1 ##NaN)` are both false
    (testing "IEEE754 special cases"
      (are [expected f col] (=-or-NaN? expected (apply min-key f col))
        ##-Inf identity [##-Inf ##Inf]
        1 identity [1 ##Inf]
        ##-Inf identity [1 ##-Inf]
        #?@(:lpy
            [##NaN identity [##NaN 1]
             1 identity [1 ##NaN]]
            :default
            [1 identity [##NaN 1]
             ##NaN identity [1 ##NaN]])
        ;; testing every permutation of -Inf, 1, and NaN
        #?@(:lpy
            [##NaN identity [##NaN ##-Inf 1]
             ##NaN identity [##NaN 1 ##-Inf]]
            :default
            [##-Inf identity [##NaN ##-Inf 1]
             ##-Inf identity [##NaN 1 ##-Inf]])
        #?@(:lpy
            [##-Inf identity [##-Inf 1 ##NaN]
             ##-Inf identity [##-Inf ##NaN 1]
             ##-Inf identity [1 ##-Inf ##NaN]
             ##-Inf identity [1 ##NaN ##-Inf]]
            :cljs
            [##NaN identity [##-Inf 1 ##NaN]
             1 identity [##-Inf ##NaN 1]
             ##NaN identity [1 ##-Inf ##NaN]
             ##-Inf identity [1 ##NaN ##-Inf]]
            :default
            [##-Inf identity [##-Inf 1 ##NaN]
             ##NaN identity [##-Inf ##NaN 1]
             ##-Inf identity [1 ##-Inf ##NaN]
             ##NaN identity [1 ##NaN ##-Inf]])))

    (testing "single argument"
      (is (= 1 (min-key identity 1)))
      (is (= 2 (min-key identity 2)))
      (is (= "x" (min-key identity "x")))
      (is (= 1 (min-key nil 1))))

    (testing "multi argument"
      (are [expected f col] (= expected (apply min-key f col))
        -3 inc [-3 -1 2]
        1  identity [1 2 3 4 5]
        1  identity [5 4 3 2 1]
        1  identity [1 2 3 4 1]
        ##-Inf identity [1 2 3 4 5 ##-Inf]
        -1 my-abs [-3 -1 2]
        -1 my-sqr [-3 -1 2 4]))

    (testing "multiple types"
      (are [expected f col] (= expected (apply min-key f col))
        "a" count ["a" "bb" "ccc"]
        "c" count ["a" "bb" "c"]
        "a" (constantly 5) [nil 1 {:k 2} [3] '(4) #{5} "a"]
        {:val 2} :val [{:val 2} {:val 3} {:val 4}]
        #?@(:lpy
            ["x" identity ["x" "y"]
             "x" identity ["y" "x" "z"]
             [1] identity [[1] [2]]
             [1] identity [[2] [1] [3]]
             #{1} identity [#{1} #{2}]
             #{2} identity [#{2} #{1} #{3}]]
            :cljs
            ["x" identity ["x" "y"]
             "x" identity ["y" "x" "z"]
             [1] identity [[1] [2]]
             [1] identity [[2] [1] [3]]
             {:val 1} identity [{:val 1} {:val 2}]
             {:val 1} identity [{:val 2} {:val 1} {:val 3}]
             #{1} identity [#{1} #{2}]
             #{1} identity [#{2} #{1} #{3}]
             nil identity  [nil nil]]
            :default [])))

    (testing "negative cases"
      (are [f col] (thrown? #?(:cljs js/Error :default Exception) (apply min-key f col))
        nil [1 2]
        nil [2 1 3]
        #?@(:cljs []
            :lpy
            [identity [{:val 1} {:val 2}]
             identity [{:val 2} {:val 1} {:val 3}]]
            :default
            [identity ["x" "y"]
             identity ["y" "x" "z"]
             identity [[1] [2]]
             identity [[2] [1] [3]]
             identity [{:val 1} {:val 2}]
             identity [{:val 2} {:val 1} {:val 3}]
             identity [#{1} #{2}]
             identity [#{2} #{1} #{3}]])))))
