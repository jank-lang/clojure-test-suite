(ns clojure.core-test.assoc-in
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/assoc-in

  ;; DOCSTRING:
  ;; clojure.core/assoc-in
  ;; [m [k & ks] v]
  ;; Associates a value in a nested associative structure, where ks is a
  ;; sequence of keys and v is the new value and returns a new nested structure.
  ;; If any levels do not exist, hash-maps will be created.

  (deftest test-assoc-in
    (testing "maps"
      (are [expected m ks v] (= expected (assoc-in m ks v))
                             {:a 1}             {}              [:a]          1
                             {:a 1}             {:a 0}          [:a]          1
                             {:a 2 :b 2}        {:a 1 :b 2}     [:a]          2
                             {:a {:b 1}}        {}              [:a :b]       1
                             {:a {:b 2} :c 3}   {:a {:b 1} :c 3} [:a :b]      2
                             {:a {:b {:c {:d 1}}}} {}           [:a :b :c :d] 1
                             {:a {:b 1}}        nil             [:a :b]       1
                             {nil 1}            {}              [nil]         1
                             {nil {nil 1}}      {}              [nil nil]     1
                             {{"a" 1} 1}        {}              [{"a" 1}]      1
                             {{:a 1} 1}         {}              [{:a 1}]      1
                             {[1 2] 1}          {}              [[1 2]]       1
                             {1 :x}             {}              [1]           :x
                             {"a" {:b 1}}       {}              ["a" :b]      1))

    (testing "maps - existing nested structure"
      (are [expected m ks v] (= expected (assoc-in m ks v))
                             {:a {:b 1}}      {:a 1}        [:a]    {:b 1}
                             {:a {:b {:c 1}}} {:a {:b {}}} [:a :b] {:c 1}
                             {:a [{:b 1}]}    {:a [{:b 0}]} [:a 0]  {:b 1}))

    (testing "vectors"
      (are [expected m ks v] (= expected (assoc-in m ks v))
                             [0]           []            [0]   0
                             [9]           [0]           [0]   9
                             [0 9]         [0 1]         [1]   9
                             [0 9 2 3]     [0 1 2 3]     [1]   9
                             [0 1 2 3 9]   [0 1 2 3]     [4]   9
                             [[9]]         [[0]]         [0 0] 9
                             [[1 2] [9 4]] [[1 2] [3 4]] [1 0] 9
                             [{:a 1}]      []            [0 :a] 1
                             [0 {:a 1}]    [0 {:a 0}]    [1 :a] 1
                             [0 [1 9]]     [0 [1 2]]     [1 1] 9))

    (testing "mixed maps and vectors"
      (is (= {:id 1
              :matrix [[0 1 2]
                       [3 4 {:var "y"}]
                       [6 7 8]]}
             (assoc-in {:id 1
                        :matrix [[0 1 2]
                                 [3 4 {:var "x"}]
                                 [6 7 8]]}
                       [:matrix 1 2 :var] "y")))

      (is (= {:a [{:b [0 9]}]}
             (assoc-in {:a [{:b [0 1]}]} [:a 0 :b 1] 9))))

    (testing "empty key sequence"
      ;; With no keys there is no level to descend to, so the value is
      ;; associated under a `nil` key at the top level.
      (are [expected m] (= expected (assoc-in m [] :v))
                        {nil :v} {}
                        {nil :v} nil
                        {:a 1 nil :v} {:a 1})

      ;; A vector only accepts integer keys.
      (is (p/thrown? (assoc-in [0] [] :v))))

    (testing "no mutation of persistent data structures"
      (let [m {:a {:b 1}}
            _ (assoc-in m [:a :b] 2)]
        (is (= {:a {:b 1}} m)))

      (let [v [0 [1 2]]
            _ (assoc-in v [1 1] 9)]
        (is (= [0 [1 2]] v))))

    (testing "meta preservation"
      (let [test-meta {:me "ta"}
            with-test-meta #(with-meta % test-meta)
            with-test-meta? #(= test-meta (meta %))]
        (are [m ks v] (with-test-meta? (assoc-in (with-test-meta m) ks v))
                      {}         [:a]     1
                      {}         [:a :b]  1
                      {:a {}}    [:a :b]  1
                      {:a [0 1]} [:a 1]   9
                      []         [0]      1
                      []         [0 :a]   1
                      [[0]]      [0 0]    9)))

    (testing "bad shape"
      (testing "vector index out of bounds"
        ;; `assoc` on a vector only supports appending at the index equal
        ;; to the current count.
        (are [m ks v] (p/thrown? (assoc-in m ks v))
                      []    [1]  1
                      []    [2]  1
                      []    [-1] 1
                      [0 1] [3]  1
                      [0 1] [-1] 1))

      (testing "non-associative level in the path"
        (are [m ks v] (p/thrown? (assoc-in m ks v))
                      {:a 1}   [:a :b] 2
                      #{0 1 2} [2]     3
                      '(0 1 2) [0]     3
                      '(0 1 2) [:a]    3))

      (testing "non-collection path"
        (are [m ks v] (p/thrown? (assoc-in m ks v))
                      {} 1  2
                      {} :a 2)))))
