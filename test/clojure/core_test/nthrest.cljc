(ns clojure.core-test.nthrest
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/nthrest
  (deftest test-nthrest
    (is (= '(3 4 5 6 7 8 9) (nthrest (range 0 10) 3)))
    (is (= [3 4 5] (nthrest [0 1 2 3 4 5] 3)))
    (is (= (range 0 10) (nthrest (range 0 10) 0)))
    (is (= '(9) (nthrest (range 0 10) 9)))
    (is (= '() (nthrest (range 0 10) 10)))
    (is (= '() (nthrest (range 0 10) 100)))
    (is (= '() (nthrest [1 2 3] 100)))
    (is (= '() (nthrest [] 100)))
    (is (= (range 3) (nthrest (range 3) -1))) ; if n < 1, returns collection unchanged

    (is (nil? (nthrest nil 0))) ; if n < 1 or (seq coll) = nil, returns collection unchanged
    #?(:cljs
       (is (nil? (nthrest nil 100)))
       :default
       (is (= '() (nthrest nil 100))))

    ;; Negative tests
    #?@(:cljs
        ;; CLJS does some nil punning to 0
        [(is (= (range 0 10) (nthrest (range 0 10) nil)))
         (is (= '(0 1 2) (nthrest [0 1 2] nil)))
         (is (nil? (nthrest nil nil)))]
        :default
        [(is (thrown? #?(:cljs :default :clj Exception) (nthrest (range 0 10) nil)))
         (is (thrown? #?(:cljs :default :clj Exception) (nthrest [0 1 2] nil)))
         (is (thrown? #?(:cljs :default :clj Exception) (nthrest nil nil)))])))
