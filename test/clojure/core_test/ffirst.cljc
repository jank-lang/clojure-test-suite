(ns clojure.core-test.ffirst
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists ffirst
  (deftest test-ffirst
    (testing "common"
      (is (= nil (ffirst '())))
      (is (= nil (ffirst [])))
      (is (= nil (ffirst {})))
      (is (= nil (ffirst #{})))
      (is (= nil (ffirst nil)))
      (is (= :a (ffirst {:a :b})))
      (is (= 0 (ffirst [[0 1] [2 3]])))
      (is (= 0 (ffirst '([0 1] [2 3]))))
      (is (= 0 (ffirst (repeat (range)))))
      (is (= 0 (ffirst [(range)])))
      (is (= \a (ffirst ["ab" "cd"])))
      (is (= \a (ffirst ["abcd"])))
      (is (= \a (ffirst #{"abcd"}))))

    (testing "exceptions"
      #?@(:cljs
          [(is (p/thrown? (ffirst (range 0 10))))
           (is (p/thrown? (ffirst (range)))) ; infinite lazy seq
           (is (p/thrown? (ffirst [:a :b :c])))
           (is (p/thrown? (ffirst '(:a :b :c))))]
          :default
          [(is (p/thrown? (ffirst (range 0 10))))
           (is (p/thrown? (ffirst (range)))) ; infinite lazy seq
           (is (p/thrown? (ffirst [:a :b :c])))
           (is (p/thrown? (ffirst '(:a :b :c))))]))))
