(ns clojure.core-test.rest
  (:require [clojure.test :as t :refer [deftest is are]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists rest
  (deftest test-rest
    (are [expected xs] (= expected (rest xs))
      '(1 2 3 4 5 6 7 8 9) (range 0 10)
      '(2 3 4 5 6 7 8 9) (rest (range 0 10)) ; do it twice
      '(2 3) [1 2 3]
      '(\e \l \l \o) "hello"
      '(1 2 3 4) (int-array (range 5))
      ;; Sorted collections not currently implemented in Basilisp
      #?@(:lpy []
          :default ['([:b 2] [:c 3]) (sorted-map :a 1 :b 2 :c 3)
                    '(:b :c) (sorted-set :a :b :c)])
      '() [1]
      '() '(1)
      '() {:a 1}
      '() #{:a}
      '() (rest "a")
      '() nil
      '() '()
      '() []
      '() "")

    (is (= 1 (first (rest (range)))))   ; infinite lazy seq
    (is (= 2 (count (rest {:a 1 :b 2 :c 3})))) ; don't know order
    (is (= 2 (count (rest #{:a :b :c})))) ; don't know order

    ;; non-seqable values throw
    (is (p/thrown? (rest 1)))
    (is (p/thrown? (rest :a)))))
