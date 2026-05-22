(ns clojure.core-test.distinct
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists distinct

  (def lots-o-dupes [1 1 1 2 1 2 1 2 3 1 2 3 1 2 3 4 1 2 3 4 5])
  (def dupes-with-meta [{:k :v} ^:whatever {:k :v}])
  (def dupes-not-identical [#{:a :b :c} (set [:a :b :c])])

  (deftest test-distinct
    ;; Make sure the objects in these collections are not identical
    (is (not (identical? (first dupes-with-meta) (second dupes-with-meta))))
    (is (not (identical? (first dupes-not-identical) (second dupes-not-identical))))
    
    (testing "arity 0 - transducer"
      (let [xform (distinct)
            s (transduce xform conj lots-o-dupes)]
        (is (fn? xform))
        (is (= [1 2 3 4 5] s)))

      ;; Odd cases check if comparison is done using `=`
      (is (= [{:k :v}] (transduce (distinct) conj dupes-with-meta)))
      (is (= [#{:a :b :c}] (transduce (distinct) conj dupes-not-identical)))

      ;; Check no dupes
      (is (= (range 10) (transduce (distinct) conj (range 10))))

      (is (= [] (transduce (distinct) conj '())))
      (is (= [] (transduce (distinct) conj nil))))

    (testing "arity 1"
      (let [s (distinct lots-o-dupes)]
        (is (p/lazy-seq? s))
        (is (not (realized? s)))
        (is (= [1 2 3 4 5] s)))

      ;; Odd cases check if comparison is done using `=`
      (is (= [{:k :v}] (distinct dupes-with-meta)))
      (is (= [#{:a :b :c}] (distinct dupes-not-identical)))

      ;; No dupes
      (is (= (range 10) (distinct (range 10))))

      ;; Check empty collections
      (is (= '() (distinct '())))
      (is (= '() (distinct nil))))))
