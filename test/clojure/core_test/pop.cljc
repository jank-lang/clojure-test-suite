(ns clojure.core-test.pop
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists pop
  (deftest test-pop
    (testing "common"
      (is (= nil (pop nil)))
      (is (= '(nil) (pop '(nil nil))))
      (is (= [1 2] (pop [1 2 3])))
      (is (= [1 2] (pop [1 2 (range)])))
      (is (= '(2 3) (pop '(1 2 3))))
      (is (= '(2 3) (pop '((range) 2 3))))
      #?@(:cljs [(is (thrown? js/Error (pop \space)))
                 (is (thrown? js/Error (pop 0)))
                 (is (thrown? js/Error (pop 0.0)))
                 (is (thrown? js/Error (pop [])))
                 (is (thrown? js/Error (pop '())))
                 (is (thrown? js/Error (pop {})))]
          :default [(is (thrown? Exception (pop 0)))
                    (is (thrown? Exception (pop 0.0)))
                    (is (thrown? Exception (pop \space)))
                    (is (thrown? Exception (pop [])))
                    (is (thrown? Exception (pop '())))
                    (is (thrown? Exception (pop {})))]))))
