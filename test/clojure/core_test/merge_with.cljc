(ns clojure.core-test.merge-with
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(defn second-arg [_a b] b)

(when-var-exists
 merge-with
 (deftest test-merge-with
   (testing "`merge-with`"

     (testing "zero arg behavior"
       #?(:clj     (is (p/thrown? (merge-with)))
          :cljs    (is (nil? (merge-with))) ; TODO how to suppress warning
          :default (is (p/thrown? (merge-with)))))

     (testing "`nil`ish behavior"
       (is (nil? (merge-with second-arg nil)))
       (is (nil? (merge-with second-arg nil nil nil)))
       (is (= {} (merge-with second-arg {} nil)))
       (is (= {} (merge-with second-arg nil {})))
       (is (= {:a 1} (merge-with second-arg {:a 1} nil {})))
       (is (= {:a 1} (merge-with second-arg {:a 1} {} nil))))

     (testing "f is applied left-to-right across maps"
       ;; str is non-commutative, so order reveals the fold direction
       (is (= {:a "123"} (merge-with str {:a "1"} {:a "2"} {:a "3"}))))

     (testing "falsey values still trigger f on collision"
       (is (= {:a false} (merge-with second-arg {:a true} {:a false})))
       (is (= {:a nil} (merge-with second-arg {:a true} {:a nil}))))

     (testing "when f returns nil, the key is kept with a nil value"
       (is (= {:a nil} (merge-with (constantly nil) {:a 1} {:a 2}))))

     (testing "f is only called on the values of duplicate keys"
       (let [called-with (atom [])
             tracking-sum (fn [a b]
                            (swap! called-with conj [a b])
                            (+ a b))]
         ;; f is never called, no conflicts
         (merge-with tracking-sum {:a 1 :b 10} {:c 100})
         (is (= [] @called-with))
         ;; f only called for values of :b
         (merge-with tracking-sum {:a 1 :b 10} {:c 100 :b 20})
         (is (= [[10 20]] @called-with))))

     (testing "non-map types"
       #?(:clj    (is (thrown-with-msg? java.lang.ClassCastException
                                        #".*cannot be cast to .*"
                                        (merge-with second-arg {:a 1} [:a 2])))
          :cljs    (is (p/thrown? (merge-with second-arg {:a 1} [:a 2])))
          :default (is (p/thrown? (merge-with second-arg {:a 1} [:a 2]))))
       #?(:clj    (is (thrown-with-msg? java.lang.IllegalArgumentException
                                        #"^Don't know how to create ISeq from.*"
                                        (merge-with second-arg {:a 1} 1)))
          :cljs    (is (p/thrown? (merge-with second-arg {:a 1} 1)))
          :default (is (p/thrown? (merge-with second-arg {:a 1} 1)))))

     (testing "duplicate mappings are handled according to f"
       (is (= {:a 2} (merge-with second-arg {:a 1} {:a 2})))
       (is (= {:a 3} (merge-with + {:a 1} {:a 2})))
       (let [val 2]
         (is (= {:a 4} (merge-with + {:a val} {:a val})))) ; not fooled by identity
       (is (= {:a 3 :b 1} (merge-with + {:a 1} {:b 1} nil {:a 2})))
       (is (= {:a [1 2] :b 3} (merge-with (fn [a b] (if (vector? a)
                                                      (conj a b)
                                                      [a b]))
                                          {:a 1} {:a 2} {:b 3}))))

     (testing "nested maps are merged with `into`"
       (is (= {:ceo {:salary 1000000, :name "Alice"},
               :cto {:salary 500000, :name "Brenda"}}
              (merge-with into
                          {:ceo {:salary 1000000}}
                          {:cto {:salary  500000}}
                          {:ceo {:name "Alice"}}
                          {:cto {:name "Brenda"}})))))))
