(ns clojure.core-test.rationalize
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists rationalize
 (deftest test-rationalize
   (are [expected x] (let [x' (rationalize x)]
                       (and (= expected x')
                            (or (integer? x')
                                (ratio? x'))))
     1                                  1
     0                                  0
     -1                                 -1
     1N                                 1N
     0N                                 0N
     -1N                                -1N
     1                                  1.0
     0                                  0.0
     -1                                 -1.0
     1                                  1.0M
     0                                  0.0M
     #?@(:cljs []
         :cljr
         [3/2                                1.5
          2476979795053773/2251799813685248  1.1
          6004799503160661/18014398509481984 (/ 1.0 3.0)
          3/2                                1.5M
          11/10                              1.1M]
         :default
         [3/2                                1.5
          11/10                              1.1
          3333333333333333/10000000000000000 (/ 1.0 3.0)
          3/2                                1.5M
          11/10                              1.1M]))))
