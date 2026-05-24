(ns clojure.core-test.set
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists set
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
      (is (= #?(:phel #{1 2} :default #{[:a 1] [:b 2]}) (set {:a 1 :b 2})))
      (is (= #{:a 1 "a" [\space]} (set [:a 1 "a" [\space]])))
      #?@(:lpy [(is (= #{\space} (set \space)))
                (is (p/thrown? (set 1)))
                (is (p/thrown? (set :a)))]
          :cljs [(is (= #{\space} (set \space)))
                 (is (p/thrown? (set 1)))
                 (is (p/thrown? (set :a)))]
          :default  [(is (p/thrown? (set 1)))
                     (is (p/thrown? (set \space)))
                     (is (p/thrown? (set :a)))]))))
