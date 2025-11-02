(ns clojure.core-test.conj
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists conj
  (deftest test-conj
    (testing "common"
      (is (= [] (conj)))
      (is (= nil (conj nil)))
      (is (= '(nil) (conj nil nil)))
      (is (= {} (conj {})))
      (is (= #{} (conj #{})))
      (is (= '() (conj '())))
      (is (= '(3) (conj nil 3)))
      (is (= '([1 2]) (conj nil [1 2])))
      (is (= [1 2 3 4] (conj [1 2 3] 4)))
      (is (= '(4 1 2 3) (conj '(1 2 3) 4)))
      (is (= '(\f \e \d \a \b \c) (conj '(\a \b \c) \d \e \f)))
      (is (= [[1 2] [3 4] [5 6]] (conj [[1 2] [3 4]] [5 6])))
      (is (= {:a 0 :b 1} (conj {:a 0} [:b 1])))
      (is (= {:a 0 :b 1} (conj {:a 0} {:b 1})))
      (is (= #{1 #{2}} (conj #{1} #{2})))
      (is (= {:a 1} (conj {:a 0} {:a 1})))
      (is (= {:a 2} (conj {:a 0} {:a 1} {:a 2})))
      (is (= ["a" "b" "c" ["d" "e" "f"]] (conj ["a" "b" "c"] ["d" "e" "f"])))

      #?@(:jank []
          :cljs [(is (thrown? js/Error (conj \a \b)))
                 (is (thrown? js/Error (conj 1 2)))
                 (is (thrown? js/Error (conj :a :b)))
                 (is (thrown? js/Error (conj {:a 0} '(:b 1))))]

          :default [(is (thrown? Exception (conj "a" "b")))
                    (is (thrown? Exception (conj \a \b)))
                    (is (thrown? Exception (conj 1 2)))
                    (is (thrown? Exception (conj :a :b)))
                    (is (thrown? Exception (conj {:a 0} '(:b 1))))]))

    (testing "meta preservation"
      (let [meta-data {:foo 42}
            apply-meta #(-> % (with-meta meta-data) (conj [:k :v]) meta)]
        (is (= meta-data (apply-meta {}) (apply-meta []) (apply-meta #{}) (apply-meta '())))))
    
    (when-var-exists clojure.core/first
                     (testing "first"
                       (is (= -1 (first (conj (range) -1))))))))
