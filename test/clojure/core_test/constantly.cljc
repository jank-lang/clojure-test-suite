(ns clojure.core-test.constantly
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists constantly
  (deftest test-constantly
    (testing "`constantly`"
      (is (fn? (constantly nil)))

      (are [v] (= v ((constantly v)))
        `sym
        "abc"
        100000
        1.0
        #?@(:cljs [] ; most Clojure dialects support ratios - not CLJS
            :default [111/7])
        \return
        nil
        true
        false
        :keyword
        :namespace/keyword
        '(one two three)
        [4 5 6]
        {:7 "8"}
        (zipmap (take 100 (range))
                (cycle ['foo 'bar 'baz 'qux]))
        #{:a :b "c"})

      (let [the-fn (constantly :foo)]
        (is (= :foo (the-fn)))
        (is (= :foo (the-fn 1)))
        (is (= :foo (the-fn :foo)))
        (is (= :foo (the-fn ::some-value)))
        (is (= :foo (the-fn :k :v)))
        (is (= :foo (the-fn 1 2 3 4 5 6 7 8 9 10)))
        (is (= :foo (the-fn "")))
        (is (= :foo (the-fn [])))
        (is (= :foo (the-fn [:x :y :z])))
        (is (= :foo (the-fn {:k :v})))
        (is (= :foo (the-fn even?)))
        (is (= :foo (the-fn (some-fn even? odd?))))
        (is (= :foo (apply the-fn (take 1000 (cycle ['foo "bar" :baz])))))
        (is (apply = (map (constantly (rand-int 100)) [:a :b :c :d :e :f])))))))
