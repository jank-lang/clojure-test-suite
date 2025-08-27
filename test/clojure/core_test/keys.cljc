(ns clojure.core-test.keys
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/keys
  (deftest test-keys
    (testing "common"
      (is (= nil (keys nil)))
      (is (= nil (keys {})))
      (is (= nil (keys [])))
      (is (= nil (keys '())))
      (is (= nil (keys #{})))
      (is (= nil (keys "")))
      (is (= '(0) (keys {0 0.0})))
      (is (= '(:a) (keys {:a :b})))
      (is (= '(:a) (keys {:a (range)})))
      (is (= '("a") (keys {"a" :b})))
      (is (= '([:a :b]) (keys {[:a :b] :c})))
      (is (= '((:a)) (keys {(keys {:a :b}) :c})))
      #?@(:cljs [(is (thrown? js/Error (keys 0)))]
	      :cljr [(is (thrown? Exception (keys 0)))]
          :clj  [(is (thrown? Exception (keys 0)))]))))
