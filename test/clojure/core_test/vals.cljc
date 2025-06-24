(ns clojure.core-test.vals
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/vals
  (deftest test-vals
    (testing "common"
      (is (= nil (vals nil)))
      (is (= nil (vals {})))
      (is (= nil (vals [])))
      (is (= nil (vals '())))
      (is (= nil (vals #{})))
      (is (= '(0.0) (vals {0 0.0})))
      (is (= '(:b) (vals {:a :b})))
      (is (= '(:b :d) (vals {:a :b :c :d})))
      (is (= '("b") (vals {"a" "b"})))
      (is (= '([:b :c]) (vals {:a [:b :c]})))
      (is (= '((:c)) (vals {:a (vals {:b :c})})))
      #?@(:cljs [(is (thrown? js/Error (vals 0)))]
          :clj  [(is (thrown? Exception (vals 0)))]))))
