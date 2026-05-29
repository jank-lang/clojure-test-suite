(ns clojure.core-test.take
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists take
  (deftest test-take
    (is (= (range 0 5) (take 5 (range 0 10))))
    (is (= (range 0 5) (take 5 (range)))) ; Infinite `range` lazy seq
    (is (= '() (take 5 nil)))
    
    ;; transducer versions
    (is (= (vec (range 0 5)) (into [] (take 5) (range 0 10))))
    (is (= (vec (range 0 5)) (into [] (take 5) (range))))
    (is (= [] (into [] (take 5) nil)))

    ;; negative tests
    (is (p/thrown? (doall (take nil (range 0 10)))))
    ;; Phel's transducer arity of `take` is lenient: a nil count yields an
    ;; empty result instead of throwing (nil is punned to "take nothing").
    ;; Documented divergence; the lazy-seq arity above still throws.
    #?(:phel (is (= [] (into [] (take nil) (range 0 10))))
       :default (is (p/thrown? (into [] (take nil) (range 0 10)))))))
