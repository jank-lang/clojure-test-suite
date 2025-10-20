(ns clojure.core-test.reverse
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists reverse
  (deftest test-reverse
    (testing "common"
      (is (= '() (reverse nil)))
      (is (= '() (reverse '())))
      (is (= '() (reverse [])))
      (is (= '(3 2 1) (reverse '(1 2 3))))
      (is (= '(3 2 1) (reverse [1 2 3])))
      (is (= '([4 5] 3 2 1) (reverse [1 2 3 [4 5]])))
      (is (= '(\c \b \a) (reverse "abc")))
      (is (= '([:a :b]) (reverse {:a :b})))
      #?@(:cljs [(is (= '(\a) (reverse \a)))
                 (is (thrown? js/Error (reverse 0)))
                 (is (thrown? js/Error (reverse 0.0)))]
          :default [(is (thrown? Exception (reverse \a)))
                    (is (thrown? Exception (reverse 0)))
                    (is (thrown? Exception (reverse 0.0)))]))))
