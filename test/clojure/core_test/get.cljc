(ns clojure.core-test.get
  (:require [clojure.test :as t :refer [are deftest]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists get
  (deftest test-get
    (let [arr (to-array [1 2])]
      ;; two-arg
      (are [expected m key] (= expected (get m key))
        nil nil nil
        nil nil :a
        nil {} :a
        nil #{} :a
        nil [] 1
        nil "" 1
        1 {:a 1 :b 2} :a
        nil {:a 1 :b 2} :z
        :a #{:a :b} :a
        nil #{:a :b} :z
        :a [:a :b] 0
        nil [:a :b] 10
        #?(:cljs "a", :default \a) "ab" 0
        nil "ab" 10
        1 arr 0
        nil arr 10)

      ;; three-arg
      (are [expected m key not-found] (= expected (get m key not-found))
        :not-found nil :a :not-found
        :not-found {} :a :not-found
        :not-found #{} :a :not-found
        :not-found [] 1 :not-found
        :not-found "" 1 :not-found
        1 {:a 1 :b 2} :a :not-found
        :not-found {:a 1 :b 2} :z :not-found
        :a #{:a :b} :a :not-found
        :not-found #{:a :b} :z :not-found
        :a [:a :b] 0 :not-found
        :not-found [:a :b] 10 :not-found
        #?(:cljs "a", :default \a) "ab" 0 :not-found
        :not-found "ab" 10 :not-found
        1 arr 0 :not-found
        :not-found arr 10 :not-found))))
