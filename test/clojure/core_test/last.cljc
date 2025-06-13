(ns clojure.core-test.last
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/last
  (deftest test-last
    (testing "common"
      (is (= 9 (last (range 0 10))))
      (is (= :c (last [:a :b :c])))
      (is (= :c (last '(:a :b :c))))
      (is (= \d (last "abcd")))
      (is (= \a (last "a")))
      (is (= nil (last '())))
      (is (= nil (last [])))
      (is (= nil (last #{})))
      (is (= nil (last nil))))

    (testing "exceptions"
      #?@(:clj 
          [(is (thrown? Exception (last \a)))
           (is (thrown? Exception (last 0)))]
          :cljs 
          [(is (thrown? js/Error (last 0)))]))))
