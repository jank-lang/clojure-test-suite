(ns clojure.core-test.contains-qmark
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/contains?
  (deftest test-contains?
    (testing "common"
      (is (= false (contains? nil nil)))
      (is (= false (contains? {} nil)))
      (is (= false (contains? [] nil)))
      ;; find by index
      (is (= true (contains? ["a" "b" "c"] 0)))
      (is (= false (contains? ["a" "b" "c"] 3)))
      (is (= true (contains? "abc" 0)))
      (is (= true (contains? "abc" 2)))
      (is (= false (contains? "abc" 3)))
      #?(:cljs (is (= false (contains? "abc" "a")))
         :clj  (is (thrown? Exception (contains? "abc" "a")))
		 :cljr (is (thrown? Exception (contains? "abc" "a"))))
      ;; find by key
      (is (= true (contains? {:a 1 :b 1} :a)))
      (is (= false (contains? {:a 1 :b 1} :c)))
      (is (= true (contains? {:a 1 :b (range)} :a))))))
