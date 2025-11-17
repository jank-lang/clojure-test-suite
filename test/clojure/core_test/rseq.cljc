(ns clojure.core-test.rseq
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists rseq
  (deftest test-rseq
    (testing "common"
      (is (= nil (rseq [])))
      (is (= '(3 2 1) (rseq [1 2 3])))
      (is (= '(:c :b :a) (rseq [:a :b :c])))
      (is (= '(\c \b \a) (rseq [\a \b \c])))
      (is (= '(4 3 [1 2]) (rseq [[1 2] 3 4])))
      #?@(:cljs [(is (thrown? js/Error (rseq nil)))
                 (is (thrown? js/Error (rseq "")))
                 (is (thrown? js/Error (rseq :a)))
                 (is (thrown? js/Error (rseq 0)))
                 (is (thrown? js/Error (rseq 0.0)))
                 (is (thrown? js/Error (rseq {:a :b})))]
          :default [(is (thrown? Exception (rseq nil)))
                    (is (thrown? Exception (rseq "")))
                    (is (thrown? Exception (rseq \space)))
                    (is (thrown? Exception (rseq :a)))
                    (is (thrown? Exception (rseq 0)))
                    (is (thrown? Exception (rseq 0.0)))
                    (is (thrown? Exception (rseq {:a :b})))]))
    
    (when-var-exists sorted-map
                     (testing "sorted-map"
                       (is (= '([:c 2] [:b 1] [:a 0]) (rseq (sorted-map :a 0, :b 1, :c 2))))))))
