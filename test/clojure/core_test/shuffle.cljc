(ns clojure.core-test.shuffle
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/shuffle
  (deftest test-shuffle
    (testing "different collection types"
      (let [x [1 2 3]
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      (let [x #{1 2 3}
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      (let [x '(1 2 3)
            actual (shuffle x)]
        (is (vector? actual))
        (is (= (count x) (count actual))))
      #?(:cljs
         (let [x "abc"
               actual (shuffle x)]
           (is (vector? actual))
           (is (= (count x) (count actual))))))
    (testing "negative cases"
      #?(:cljs (is (thrown? js/Error (shuffle 1)))
         :default (is (thrown? Exception (shuffle 1))))
      #?@(:cljs
          [(is (= [] (shuffle nil)))
           (is (= [] (shuffle {})))]
          :cljr
          [(is (thrown? Exception (shuffle nil)))
           (is (thrown? Exception (shuffle "abc")))
           (is (= [] (shuffle {})))]
          :default
          [(is (thrown? Exception (shuffle nil)))
           (is (thrown? Exception (shuffle "abc")))
           (is (thrown? Exception (shuffle {})))]))))
