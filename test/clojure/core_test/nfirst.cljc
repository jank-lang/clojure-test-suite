(ns clojure.core-test.nfirst
  (:require clojure.core
            [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists nfirst
  (deftest test-nfirst
    (testing "common"
      (is (= nil (nfirst '())))
      (is (= nil (nfirst [])))
      (is (= nil (nfirst {})))
      (is (= nil (nfirst #{})))
      (is (= nil (nfirst nil)))
      (is (= nil (nfirst "")))
      (is (= '(:b) (nfirst {:a :b})))
      (is (= '(1) (nfirst [[0 1] [2 3]])))
      (is (= '(1) (nfirst '([0 1] [2 3]))))
      (is (= '(1 2 3 4) (nfirst (repeat (range 0 5)))))
      (is (= '(\b) (nfirst ["ab" "cd"])))
      (is (= '(\b \c \d) (nfirst ["abcd"])))
      (is (= '(\b \c \d) (nfirst #{"abcd"}))))
    
    (testing "exceptions"
      #?@(:cljs
          [(is (p/thrown? (nfirst (range 0 10))))
           (is (p/thrown? (nfirst (range))))
           (is (p/thrown? (nfirst [:a :b :c])))
           (is (p/thrown? (nfirst '(:a :b :c))))]
          :default
          [(is (p/thrown? (nfirst (range 0 10))))
           (is (p/thrown? (nfirst (range))))
           (is (p/thrown? (nfirst [:a :b :c])))
           (is (p/thrown? (nfirst '(:a :b :c))))]))))
