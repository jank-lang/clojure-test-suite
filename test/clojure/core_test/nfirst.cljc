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
          [(is (thrown? js/Error (nfirst (range 0 10))))
           (is (thrown? js/Error (nfirst (range))))
           (is (thrown? js/Error (nfirst [:a :b :c])))
           (is (thrown? js/Error (nfirst '(:a :b :c))))]
          :default
          [(is (thrown? Exception (nfirst (range 0 10))))
           (is (thrown? Exception (nfirst (range))))
           (is (thrown? Exception (nfirst [:a :b :c])))
           (is (thrown? Exception (nfirst '(:a :b :c))))]))))
