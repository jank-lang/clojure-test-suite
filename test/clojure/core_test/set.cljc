(ns clojure.core-test.set
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/set
  (deftest test-set
    (testing "common"
      (is (= #{} (set nil)))
      (is (= #{} (set [])))
      (is (= #{} (set '())))
      (is (= #{\a \b \c} (set "abc")))
      (is (= #{} (set #{})))
      (is (= #{:a} (set #{:a})))
      (is (= #{1 2 3} (set [1 1 2 2 3 3 3])))
      (is (= #{:a 1 "a"} (set '(:a 1 "a"))))
      (is (= #{:a 1 "a"} (set [:a 1 "a"])))
      (is (= #{:a 1 "a" [\space]} (set [:a 1 "a" [\space]])))
      #?@(:clj  [(is (thrown? Exception (set 1)))
                 (is (thrown? Exception (set \space)))
                 (is (thrown? Exception (set :a)))]
          :cljs [(is (= #{\space} (set \space)))
                 (is (thrown? js/Error (set 1)))
                 (is (thrown? js/Error (set :a)))]))))
