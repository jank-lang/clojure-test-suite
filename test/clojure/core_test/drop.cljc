(ns clojure.core-test.drop
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/drop
  (deftest test-drop
    (is (= (range 1 10) (drop 1 (range 0 10))))
    (is (= (range 5 10) (drop 5 (range 0 10))))
    (is (= 5 (first (drop 5 (range))))) ; lazy version handles infinite `range`
    (is (= '() (drop 5 nil)))           ; nil acts as empty list

    ;; Transducer version
    (is (= (vec (range 5 10)) (into [] (drop 5) (range 0 10))))

    ;; Note that we can drop from other types of collections, but
    ;; because they are not sequential, we don't know exactly what the
    ;; result will be. This tests that something truthy remained after
    ;; dropping one item and that `drop` didn't throw when given maps
    ;; or sets. We need `doall` here to force the realization of the
    ;; lazy seq created by `drop`.
    (is (doall (drop 1 {:a 1 :b 2 :c 3})))
    (is (doall (drop 1 #{:a :b :c})))

    ;; Negative tests
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                 (doall (drop nil (range 0 10)))))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                 (into [] (drop nil) (range 0 10))))))
