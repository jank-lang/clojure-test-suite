(ns clojure.core-test.butlast
  (:require [clojure.test :as t :refer [deftest is are]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists butlast
  (deftest test-butlast
    (are [expected x] (= expected (butlast x))
      nil (range 1)
      (range 1) (range 2)
      (range 2) (range 3)
      (range 3) (range 4)
      '(1 2) [1 2 3]
      '(1 2) (int-array [1 2 3])
      '(\a \b) "abc"
      ;; Sorted collections not currently implemented in Basilisp
      #?@(:lpy []
          :default ['([:a 1] [:b 2]) (sorted-map :a 1 :b 2 :c 3)
                    '(:a :b) (sorted-set :a :b :c)])
      nil '(0)
      nil [0]
      nil '()
      nil []
      nil nil)

    (is (= 2 (count (butlast {:a 1 :b 2 :c 3})))) ; order not fixed, so just count
    (is (= 2 (count (butlast #{:a :b :c}))))

    (is (p/thrown? (butlast 1)))
    (is (p/thrown? (butlast :a)))))
