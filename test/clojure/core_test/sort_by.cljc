(ns clojure.core-test.sort-by
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists sort-by
  ;; Data for simple tests
  (def simple-vec-maps [{:a 2 :b 9}
                        {:a 1 :b 10}
                        {:a 4 :b 7}
                        {:a 3 :b 8}])

  ;; Data for stable sorting
  (def stable-data [{:a 1 :b 11}
                    {:a 1 :b 21}
                    {:a 1 :b 39}
                    {:a 1 :b 2}
                    {:a 4 :b 5}
                    {:a 2 :b 101}
                    {:a 2 :b 71}
                    {:a 2 :b 28}
                    {:a 2 :b 99}
                    {:a 4 :b 210}
                    {:a 3 :b 113}
                    {:a 3 :b 412}
                    {:a 3 :b 135}
                    {:a 3 :b 314}
                    {:a 4 :b 215}])
  
  (deftest test-sort-by
    (testing "sort-by artity-2"
      (testing "key-fn is `identity`"
        (are [expected x] (= expected (sort x) (sort-by identity x))
          ;; cases from `sort` tests, since (sort ...) = (sort-by identity ...)
          '() nil
          '(1 2 3 4) [3 1 2 4]
          '(nil 2 3 4) [3 nil 2 4]
          '(1 2 3 4) '(3 1 2 4)
          '(1 2 3 4) #{3 1 2 4}
          '([:a 1] [:b 2] [:c 3]) {:c 3 :b 2 :a 1}
          '([1] [2] [3] [4]) [[3] [1] [2] [4]]
          '("a" "b" "c" "d") ["b" "a" "c" "d"]
          '(\c \e \j \l \o \r \u) "clojure"))
      (testing "key-fn is keyword"
        (is (= (seq [{:a 4 :b 7}
                     {:a 3 :b 8}
                     {:a 2 :b 9}
                     {:a 1 :b 10}])
               (sort-by :b simple-vec-maps)))
        (is (= (seq [{:a 1 :b 10}
                     {:a 2 :b 9}
                     {:a 3 :b 8}
                     {:a 4 :b 7}])
               (sort-by :a simple-vec-maps)))
        ;; test more complex key-fn and stability of sort-by
        ;; :a + :b = 11 for all elements of simple-vec-maps
        (is (= (seq simple-vec-maps)
               (sort-by #(+ (:a %) (:b %)) simple-vec-maps)))
        ;; If the key-fn returns the same value all the time we also
        ;; expect no change. For instance, when the key-fn returns
        ;; `nil` constantly because we use the wrong keyword or we
        ;; navigate the structure of the element incorrectly.
        (is (= (seq simple-vec-maps)
               (sort-by :c simple-vec-maps)))))
    
    (testing "sort-by artity-3"
      ;; Use `compare`
      (is (= (seq [{:a 1 :b 10}
                   {:a 2 :b 9}
                   {:a 3 :b 8}
                   {:a 4 :b 7}])
             (sort-by :a compare simple-vec-maps)))
      ;; Reverse `compare`
      (is (= (seq [{:a 4 :b 7}
                   {:a 3 :b 8}
                   {:a 2 :b 9}
                   {:a 1 :b 10}])
             (sort-by :a #(compare %2 %1) simple-vec-maps)))
      (is (= (seq [{:a 1 :b 10}
                   {:a 2 :b 9}
                   {:a 3 :b 8}
                   {:a 4 :b 7}])
             (sort-by :a < simple-vec-maps)))
      (is (= (seq [{:a 4 :b 7}
                   {:a 3 :b 8}
                   {:a 2 :b 9}
                   {:a 1 :b 10}])
             (sort-by :a > simple-vec-maps)))

      ;; Use `compare`
      (is (= (seq [{:a 4 :b 7}
                   {:a 3 :b 8}
                   {:a 2 :b 9}
                   {:a 1 :b 10}])
             (sort-by :b compare simple-vec-maps)))
      ;; Reverse `compare`
      (is (= (seq [{:a 1 :b 10}
                   {:a 2 :b 9}
                   {:a 3 :b 8}
                   {:a 4 :b 7}])
             (sort-by :b #(compare %2 %1) simple-vec-maps)))
      (is (= (seq [{:a 4 :b 7}
                   {:a 3 :b 8}
                   {:a 2 :b 9}
                   {:a 1 :b 10}])
             (sort-by :b < simple-vec-maps)))
      (is (= (seq [{:a 1 :b 10}
                   {:a 2 :b 9}
                   {:a 3 :b 8}
                   {:a 4 :b 7}])
             (sort-by :b > simple-vec-maps))))
    
    (testing "negative cases"
      ;; key-fn is not a fn
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by nil simple-vec-maps)))
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by [] simple-vec-maps)))
      ;; comparator is not a fn
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by :a nil simple-vec-maps)))
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by :a [] simple-vec-maps)))
      ;; collection is not a collection
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by :a 1)))
      (is (thrown? #?(:cljs :default, :default Exception) (sort-by :a true))))
    
    (testing "stable sort"
      ;; sort first by :b and then by :a results in runs of :a in
      ;; order with all the :b's associated with a given :a being in
      ;; order
      (is (= (seq [{:a 1, :b 2}
                   {:a 1, :b 11}
                   {:a 1, :b 21}
                   {:a 1, :b 39}
                   {:a 2, :b 28}
                   {:a 2, :b 71}
                   {:a 2, :b 99}
                   {:a 2, :b 101}
                   {:a 3, :b 113}
                   {:a 3, :b 135}
                   {:a 3, :b 314}
                   {:a 3, :b 412}
                   {:a 4, :b 5}
                   {:a 4, :b 210}
                   {:a 4, :b 215}])
             (->> stable-data
                  (sort-by :b)
                  (sort-by :a)))))))
