(ns clojure.core-test.select-keys
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/select-keys
  (deftest test-select-keys
    (testing "common"
      (is (= {} (select-keys nil nil)))
      (is (= {} (select-keys {} nil)))
      (is (= {} (select-keys {} {})))
      (is (= {} (select-keys #{} {})))
      (is (= {} (select-keys #{1} [])))
      (is (= {} (select-keys {:a "a" :b "b"} [])))
      (is (= {} (select-keys {:a "a" :b "b"} [:c])))
      (is (= {:a "a"} (select-keys {:a "a" :b "b"} [:a])))
      (is (= {:a "a"} (select-keys {:a "a" :b (range)} [:a])))
      #?@(:cljs [(is (= {} (select-keys "" [:a])))
                 (is (= {} (select-keys 0 [:a])))
                 (is (thrown? js/Error (select-keys {} :a)))]
		  :cljr [(is (= {} (select-keys "" [:a])))
                 (is (= {}  (select-keys 0 [:a])))
                 (is (thrown? Exception (select-keys {} :a)))]
          :clj  [(is (thrown? Exception (select-keys "" [:a])))
                 (is (thrown? Exception (select-keys 0 [:a])))
                 (is (thrown? Exception (select-keys {} :a)))]))))
