(ns clojure.core-test.update-in
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/update-in

  ;; DOCSTRING:
  ;; clojure.core/update-in
  ;; [m ks f & args]
  ;; 'Updates' a value in a nested associative structure, where ks is a
  ;; sequence of keys and f is a function that will take the old value
  ;; and any supplied args and return the new value, and returns a new
  ;; nested structure.  If any levels do not exist, hash-maps will be
  ;; created.

  (deftest test-update-in
    (testing "updates the value found at ks"
      (are [expected m ks] (= expected (update-in m ks inc))
        {:k 6}          {:k 5}           [:k]
        {:a {:b 2}}     {:a {:b 1}}      [:a :b]
        {:a {:b {:c 2}}} {:a {:b {:c 1}}} [:a :b :c]
        ;; the keys off the path are left alone
        {:a {:b 2} :c 3} {:a {:b 1} :c 3} [:a :b]
        [1 1]           [0 1]            [0]
        [0 2]           [0 1]            [1]
        [[0 1] [2 4]]   [[0 1] [2 3]]    [1 1]
        [{:a 2}]        [{:a 1}]         [0 :a]
        {:a [{:b 2}]}   {:a [{:b 1}]}    [:a 0 :b]))

    (testing "f receives the old value"
      (are [expected m ks f] (= expected (update-in m ks f))
        {:k 5}      {:k 5} [:k] identity
        {:k true}   {:k nil} [:k] nil?
        ;; including nil for a key that is not there
        {:k nil}    {}     [:k] identity
        {:k true}   {}     [:k] nil?
        {:k nil}    nil    [:k] identity
        {:k true}   nil    [:k] nil?
        {:a {:b true}} {}  [:a :b] nil?
        ;; a vector's index equal to its count appends, the old value is nil
        [0 1 nil]   [0 1]  [2] identity
        [0 1 true]  [0 1]  [2] nil?
        [nil]       []     [0] identity
        ;; the value f returns is what ends up in the structure
        {:k nil}    {:k 5} [:k] (constantly nil)
        {:a :x}     {:a :y} [:a] (constantly :x)))

    (testing "extra args are passed to f after old value"
      (are [expected m ks f args] (= expected (apply update-in m ks f args))
        ;; the old value is the first argument
        {:k 2}        {:k 5}      [:k]    - [3]
        {:k -2}       {:k 5}      [:k]    - [3 4]
        {:a {:k 300}} {:a {:k 5}} [:a :k] * [3 4 5]
        {:a {:k 11}}  {:a {:k 5}} [:a :k] + [1 2 3]
        {:k [5 3 4]}  {:k 5}      [:k]    vector [3 4]
        ;; a level that does not exist yet also gets the extra args
        {:k 1}        {}          [:k]    (fnil inc 0) []
        [0 1 [2]]     [0 1]       [2]     (fnil conj []) [2]))

    (testing "ks may be any kind of sequence"
      (let [m {:a {:b 1}}]
        (are [ks] (= {:a {:b 2}} (update-in m ks inc))
          [:a :b]
          '(:a :b)
          (list :a :b)
          (map identity [:a :b]))))

    (testing "missing levels are created as hash-maps"
      (are [expected m ks f] (= expected (update-in m ks f))
        {:a {:b 1}}           {}  [:a :b] (constantly 1)
        {:a {:b {:c {:d 1}}}} {}  [:a :b :c :d] (constantly 1)
        {:a {:b 1}}           nil [:a :b] (constantly 1)
        ;; a level that exists but does not have the next key yet
        {:a {:b 1}}      {:a {}}     [:a :b] (constantly 1)
        {:a {:b {:c 1}}} {:a {:b {}}} [:a :b :c] (constantly 1))
      ;; The created levels are maps rather than, say, vectors.
      (is (map? (:a (update-in {} [:a :b] identity))))
      (is (map? (:a (update-in nil [:a :b] identity)))))

    (testing "empty key sequence"
      ;; With no keys there is no level to descend to, so f is applied to
      ;; the whole collection, which is then associated under a `nil` key
      ;; at the top level.
      (is (= {:a 1 nil :v} (update-in {:a 1} [] (constantly :v))))
      (is (= {nil nil} (update-in nil [] identity)))
      ;; A vector only accepts integer keys.
      (is (p/thrown? (update-in [0] [] identity))))

    (testing "bad shape"
      (testing "a level on the path is not associative"
        (are [m ks] (p/thrown? (update-in m ks identity))
          ;; `:a` holds a number
          {:a 1}      [:a :b]
          ;; a string can be gotten into but not associated into
          {:a "hi"}   [:a 0]
          ;; the element of a vector is a number
          [{:a 1} 2]  [1 :a]))

      (testing "m is not associative"
        (are [m] (p/thrown? (update-in m [:a] identity))
          1
          3.14
          "hi"
          '(0 1 2)
          #{0 1 2}
          :k))

      (testing "vector index"
        (testing "past the end"
          (are [m ks] (p/thrown? (update-in m ks identity))
            []    [1]
            []    [2]
            [0 1] [3]))
        (testing "negative"
          (is (p/thrown? (update-in [0 1] [-1] identity))))
        (testing "non-integer"
          (is (p/thrown? (update-in [1 2 3] [:k] identity)))
          (is (p/thrown? (update-in [1 2 3] ["k"] identity)))))

      (testing "f is not callable"
        (are [f] (p/thrown? (update-in {:k 5} [:k] f))
          1
          nil
          "not-a-fn")))))
