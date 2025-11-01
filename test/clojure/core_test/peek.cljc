(ns clojure.core-test.peek
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]])
  #?(:clj  (:import [clojure.lang PersistentQueue])
     :cljr (:import [clojure.lang PersistentQueue])))

(when-var-exists clojure.core/peek
  (deftest test-peek

    (testing "list and queue"
      (is (nil? (peek '())))
      (is (= :a (peek '(:a :b :c))))
      #?(:cljs    (is (nil? (peek #queue [])))
         :default (is (nil? (peek PersistentQueue/EMPTY))))
      #?(:cljs    (is (= :a (peek #queue [:a :b :c])))
         :default (is (= :a (peek (conj PersistentQueue/EMPTY :a :b :c))))))

    (testing "vector"
      (is (nil? (peek [])))
      (is (= :c (peek [:a :b :c]))))

    (testing "nil"
      (is (nil? (peek nil))))

    (testing "bad shape"
      (are [coll] (thrown? #?(:cljs js/Error :default Exception) (peek coll))
                  #{1 2 3}
                  {:a 1 :b 2}
                  (cons 1 '())
                  (range 10)
                  "str"
                  42))))
