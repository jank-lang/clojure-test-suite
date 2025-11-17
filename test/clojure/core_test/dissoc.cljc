(ns clojure.core-test.dissoc
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists dissoc

  (defrecord TestDissocRecord [a b c])

  (deftest test-dissoc

    (testing "non-nil"
      (are [expected m keys] (= expected (apply dissoc m keys))
                             {} {} []
                             {:a 1} {:a 1} []
                             {} {:a 1} [:a]
                             {} {:a 1} [:a :a]
                             {} {:a 1 :b 2} [:a :b]
                             {:b 2} {:a 1 :b 2} [:a]
                             {:b 2} {:a 1 :b 2} [:a :c]))

    (testing "nil"
      (are [expected m keys] (= expected (apply dissoc m keys))
                             nil nil [nil]
                             {} {} [nil]
                             {} {nil nil} [nil]
                             {} {nil nil} [nil nil]))

    (testing "sorted preservation"
      (is (sorted? (dissoc (sorted-map :a 1 :b 2) :a))))

    (testing "meta preservation"
      (let [test-meta {:me "ta"}
            with-test-meta #(with-meta % test-meta)
            with-test-meta? #(= test-meta (meta %))]
        (is (with-test-meta? (dissoc (with-test-meta {:a 1 :b 2}) :a)))))

    (testing "records"
      (let [r (TestDissocRecord. 1 2 nil)]
        (are [expected keys] #?(; bb preserves the record type even if a basis fields is dissociated
                                ; https://github.com/babashka/babashka/issues/1886
                                :bb      (= expected (into {} (apply dissoc r keys)))
                                ; other implementations return a map
                                :default (= expected (apply dissoc r keys)))
                             {:b 2 :c nil} [:a]
                             {:b 2 :c nil} [:a :d]
                             {} [:a :b :c])
        ; all implementations preserve the record type if no basis field is dissociated
        (is (= r (dissoc r :d)))))

    (testing "bad shape"
      (are [m keys] (thrown? #?(:cljs js/Error :default Exception) (apply dissoc m keys))
                    42 [4]
                    '() [0]
                    [] [0]
                    #{:a :b} [:a]
                    "string" [\s \t]))))
