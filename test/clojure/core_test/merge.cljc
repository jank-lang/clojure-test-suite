(ns clojure.core-test.merge
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/merge
  (deftest test-merge
    (testing "`merge`"
      (testing "`nil` and empty map behavior"
        (is (nil? (merge)))
        (is (nil? (merge nil)))
        (is (nil? (merge nil nil)))
        (is (nil? (merge nil nil nil)))

        (is (= {} (merge {})))
        (is (= {} (merge {} nil)))
        (is (= {} (merge nil {})))
        (is (= {} (merge nil {} nil)))

        (is (= {[2 3] :foo} (merge {[2 3] :foo} nil {})))
        (is (= {1 11} (merge {1 11} {} nil))))

      (testing "lattermost mapping wins"
        (is (= {:a "aaaaa"} (merge {:a "a"} {:a "aaaaa"})))
        (is (= {:a "a" :b "b"} (merge {:a "aaaa"} {:a "a" :b "b"})))
        (is (= {:a "a" :b "b"} (merge {:a "aaaa"}
                                      {:a "a" :b "bbbb"}
                                      {:a "a" :b "b"})))
        (is (= {:a nil :b "b" :c "c"} (merge {:a "aaaa"}
                                             {:a "a" :b "bbbb" :c "c"}
                                             {:a nil :b "b"})))
        (is (= {:x 1 :y 10 :z 100} (merge {:x 1 :y 5555}
                                          {:y 10 :z 100}))))

      (testing "nested maps are replaced, not 'deep-merged'"
        (is (= {:ceo {:name "Alice"},
                :cto {:name "Brenda"}}
               (merge {:ceo {:salary 1000000}} ; salary values are overwritten
                      {:cto {:salary  500000}}
                      {:ceo {:name "Alice"}}
                      {:cto {:name "Brenda"}}))))

      (testing "map entries are accepted in position 2+, per `conj`"
        (is (= {:a "a", :b "b"} (merge {:a nil}
                                       (first {:a "a"})
                                       {:b "b"}))))

      (testing "vectors in position 2+ are treated as map-entries, per `conj`"
        (is (thrown? #?(:cljs :default, :clj java.lang.IllegalArgumentException, :clr Exception)
                     (merge {} [])))
        (is (thrown? #?(:cljs :default, :clj java.lang.IllegalArgumentException, :clr Exception)
                     (merge {} [:foo])))
        (is (= {:foo "foo"} (merge {} [:foo "foo"])))
        (is (= {"x" 1} (merge {} ["x" 1])))
        (is (= {'x 10, 'y 10} (merge {'x 10} ['y 10])))
        (testing "In CLJS (unlike other dialects) vectors with >2 arguments are treated as map-entries (where the latter values are ignored)"
          #?(:cljs (is (= {:foo :bar} (merge {} [:foo :bar :baz]))),
             :clj (is (thrown? java.lang.IllegalArgumentException (merge {} [:foo :bar :baz]))),
             :clr (is (thrown? Exception (merge {} [:foo :bar :baz])))))

        (is (= {:foo "foo", :bar "bar"} (merge {} [:foo "foo"] [:bar "bar"])))
        (is (= {'x 10, 'y 10, 'z 10} (merge {'x 10} ['y 10] ['z 10])))
        (testing "In CLJS (unlike other dialects) vectors with >2 arguments are treated as map-entries (where the latter values are ignored)"
          #?(:cljs (is (= {:foo :bar} (merge {} [:foo :bar :baz :bar]))),
             :clj (is (thrown? java.lang.IllegalArgumentException (merge {} [:foo :bar :baz :bar]))),
             :clr (is (thrown? Exception (merge {} [:foo :bar :baz :bar]))))))

      (testing "atomic values in position 2+ throw"
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge {} 1)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge {} 1 2)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge {} :foo)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge {} "str"))))

      (testing "undefined `merge` behavior on non-maps"
        ;; Behavior for non-map input is undefined. We intentionally do not test
        ;; it closely.
        (is (any? (merge '(1 2 3) 1)))
        (is (any? (merge [1 2] 3 4 5)))
        (is (any? (merge [] nil {} 1 {:a "c"})))
        (is (any? (merge (first {:a "a"}) {:b "b"} {:c "c"})))
        (is (= [:foo] (merge [:foo])))
        (is (= :foo (merge :foo)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge :foo :bar)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge 100 :foo)))
        (is (thrown? #?(:cljs :default, :clj Exception, :clr Exception)
                     (merge "str" :foo)))        
        (is (thrown?  #?(:cljs :default, :clj Exception, :clr Exception)
                      (merge nil (range))))
        (is (thrown?  #?(:cljs :default, :clj Exception, :clr Exception)
                      (merge {} '(1 2))))
        (is (thrown?  #?(:cljs :default, :clj Exception, :clr Exception)
                      (merge {} 1 2)))))))
