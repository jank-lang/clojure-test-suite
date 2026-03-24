(ns clojure.core-test.pop
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists pop
  (deftest test-pop
    (testing "common"
      (is (= nil (pop nil)))
      (is (= '(nil) (pop '(nil nil))))
      (is (= [1 2] (pop [1 2 3])))
      (is (= [1 2] (pop [1 2 (range)])))
      (is (= '(2 3) (pop '(1 2 3))))
      (is (= '(2 3) (pop '((range) 2 3))))
      #?@(:cljs [(is (p/thrown? (pop \space)))
                 (is (p/thrown? (pop 0)))
                 (is (p/thrown? (pop 0.0)))
                 (is (p/thrown? (pop [])))
                 (is (p/thrown? (pop '())))
                 (is (p/thrown? (pop {})))]
          :default [(is (p/thrown? (pop 0)))
                    (is (p/thrown? (pop 0.0)))
                    (is (p/thrown? (pop \space)))
                    (is (p/thrown? (pop [])))
                    (is (p/thrown? (pop '())))
                    (is (p/thrown? (pop {})))]))))
