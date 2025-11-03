(ns clojure.core-test.assoc-bang
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists assoc!

  (deftest test-assoc!

    (testing "maps"
      (testing "maps - single value"
        (are [expected map key val] (= expected (persistent! (assoc! (transient map) key val)))
                                    {:a 1} {} :a 1
                                    {:a 1 :b 2} {:a 1} :b 2
                                    {:a 3 :b 2} {:a 1 :b 2} :a 3))
      (testing "maps - multiple values"
        (are [expected map kvs] (= expected (persistent! (apply assoc! (transient map) kvs)))
                                {:a 1 :b 2} {} [:a 1 :b 2]
                                {:a 1 :b 3} {} [:a 1 :b 2 :b 3]
                                {:a 1 :b 3 :c 5 :d 7} {:a 1 :b 2} [:b 3 :c 5 :d 7])))

    (testing "vectors"
      (testing "vectors - single value"
        (are [expected vec index val] (= expected (persistent! (assoc! (transient vec) index val)))
                                      [nil] [] 0 nil
                                      [0] [] 0 0
                                      [0 3 2] [0 1 2] 1 3
                                      [0 1 2 3] [0 1 2] 3 3))
      (testing "vectors - multiple values"
        ; assoc coll index-1 val-1 index-2 val-2 ...
        (are [expected vec ivs] (= expected (persistent! (apply assoc! (transient vec) ivs)))
                                [1 nil] [] [0 1 1]
                                [1 2] [] [0 1 1 2]
                                [1 3 5 7] [1 2] [1 3 2 5 3 7]))
      (testing "vectors - out-of-bounds indices"
        (are [vec ivs] (thrown? #?(:cljs js/Error :default Exception) (apply assoc! (transient vec) ivs))
                       [] [-1 0]
                       [] [1 0]
                       [0 1 2] [-1 -1]
                       [0 1 2] [4 4]
                       [1 2] [1 3 3 5]
                       [1 2] [-1 3 2 5]
                       [1 2] [-1 3 3 5])))

    (testing "odd number of args"
      ; on the contrary to assoc, assoc! accepts an odd number (> 1) of args and assumes missing value is nil
      (are [coll kvs] (= (apply assoc coll (conj kvs nil)) (persistent! (apply assoc! (transient coll) kvs)))
                      {:a 1} [:b 2 :c]
                      {:a 1} [:b 2 :c 3 :d]
                      [1] [0 1 1]
                      [1] [0 1 1 2 2]))

    (testing "bad shape"
      (are [coll] (thrown? #?(:cljs js/Error :default Exception) (assoc! coll 1 3))
                  nil
                  {:a 1 :b 2}
                  [0 1 2]
                  '(0 1 2)
                  (transient '(0 1 2))
                  #{0 1 2}
                  (transient #{0 1 2})
                  true
                  false
                  :k
                  42
                  3.14))))
