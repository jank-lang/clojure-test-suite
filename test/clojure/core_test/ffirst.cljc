(ns clojure.core-test.ffirst
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/ffirst
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
      #?@(:clj 
          [(is (thrown? Exception (ffirst (range 0 10))))
               (is (thrown? Exception (ffirst (range))))          ; infinite lazy seq
               (is (thrown? Exception (ffirst [:a :b :c])))
               (is (thrown? Exception (ffirst '(:a :b :c))))]
              :cljs 
              [(is (thrown? js/Error (ffirst (range 0 10))))
               (is (thrown? js/Error (ffirst (range))))          ; infinite lazy seq
               (is (thrown? js/Error (ffirst [:a :b :c])))
               (is (thrown? js/Error (ffirst '(:a :b :c))))]))))
