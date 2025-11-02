(ns clojure.core-test.assoc
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists assoc
  (deftest test-assoc

    (testing "maps"
      (testing "maps - single value"
        (are [expected map key val] (= expected (assoc map key val))
                                    {nil nil} nil nil nil
                                    {:a 1} nil :a 1
                                    {:a 1} {} :a 1
                                    {:a 1 :b 2} {:a 1} :b 2
                                    {:a 3 :b 2} {:a 1 :b 2} :a 3))
      (testing "maps - multiple values"
        (are [expected map kvs] (= expected (apply assoc map kvs))
                                {:a 1 :b 2} nil [:a 1 :b 2]
                                {:a 1 :b 2} {} [:a 1 :b 2]
                                {:a 1 :b 3} {} [:a 1 :b 2 :b 3]
                                {:a 1 :b 3 :c 5 :d 7} {:a 1 :b 2} [:b 3 :c 5 :d 7]))
      (testing "maps - sorted type preservation"
        (is (sorted? (assoc (sorted-map) :a 1 :b 2)))
        (is (sorted? (assoc (sorted-map :a 1 :b 2) :b 3)))))

    (testing "vectors"
      (testing "vectors - single value"
        (are [expected vec index val] (= expected (assoc vec index val))
                                      [nil] [] 0 nil
                                      [0] [] 0 0
                                      [0 3 2] [0 1 2] 1 3
                                      [0 1 2 3] [0 1 2] 3 3))
      (testing "vectors - multiple values"
        ; assoc coll index-1 val-1 index-2 val-2 ...
        (are [expected vec ivs] (= expected (apply assoc vec ivs))
                                [1] [] [0 1]
                                [1 2] [] [0 1 1 2]
                                [1 3 5 7] [1 2] [1 3 2 5 3 7]))
      (testing "vectors - out-of-bounds indices"
        (are [vec ivs] (thrown? #?(:cljs js/Error :default Exception) (apply assoc vec ivs))
                       [] [-1 0]
                       [] [1 0]
                       [0 1 2] [-1 -1]
                       [0 1 2] [4 4]
                       [1 2] [1 3 3 5]
                       [1 2] [-1 3 2 5]
                       [1 2] [-1 3 3 5])))

    (testing "meta preservation"
      (let [test-meta {:me "ta"}
            with-test-meta #(with-meta % test-meta)
            with-test-meta? #(= test-meta (meta %))]
        (are [coll kvs] (with-test-meta? (apply assoc (with-test-meta coll) kvs))
                        {} [:a 1]
                        {:a 1} [:a 3 :b 5]
                        [] [0 1]
                        [1] [0 3 1 5])))

    (testing "bad shape"
      (testing "bad shape - odd number of args"
        (are [coll kvs] #?(; cljs seems to tolerate odd number of args and assume that missing value is nil
                           :cljs    (= (apply assoc coll (conj kvs nil)) (apply assoc coll kvs))
                           ; other implementations throw
                           :default (thrown? Exception (apply assoc coll kvs)))
                        {:a 1} [:b]
                        {:a 1} [:b 2 :c]
                        {:a 1} [:b 2 :c 3 :d]
                        [1] [0]
                        [1] [0 1 1]
                        [1] [0 1 1 2 2]))
      (testing "bad shape - not a map nor a vector"
        (are [coll] (thrown? #?(:cljs js/Error :default Exception) (assoc coll 1 3))
                    '(0 1 2)
                    #{0 1 2}
                    true
                    false
                    :k
                    42
                    3.14)))))
