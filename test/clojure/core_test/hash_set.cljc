(ns clojure.core-test.hash-set
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/hash-set
  (deftest test-hash-set
    (testing "common"
      (is (= #{} (hash-set)))
      (is (= #{:a} (hash-set :a)))
      (is (= #{"a"} (hash-set "a")))
      (is (= #{1} (hash-set 1 1)))
      (is (= #{nil} (hash-set nil)))
      (is (= #{\space} (hash-set \space)))
      (is (= #{'()} (hash-set '())))
      (is (= #{[]} (hash-set [])))
      (is (= #{'(1 2 3)} (hash-set '(1 2 3))))
      (is (= #{[1 2 3]} (hash-set [1 2 3])))
      (is (= #{nil #{}} (hash-set nil (hash-set)))))))
