(ns clojure.core-test.drop-while
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/drop-while
  (deftest test-drop-while
    (is (= (range 5 10) (drop-while #(< % 5) (range 0 10))))
    (is (= 5 (first (drop-while #(< % 5) (range)))))  ; lazy infinite `range`
    (is (= '(1 2 3) (drop-while keyword? [:a :b :c 1 2 3])))
    (is (= '() (drop-while #(< % 5) nil)))

    ;; Transducer
    (is (= (vec (range 5 10)) (into [] (drop-while #(< % 5)) (range 0 10))))
    (is (= [1 2 3] (into [] (drop-while keyword?) [:a :b :c 1 2 3])))
    (is (= [] (into [] (drop-while #(< % 5)) nil)))

    ;; Negative tests
    (is (thrown? #?(:cljs :default :clj Exception)
                 (doall (drop-while nil (range 0 10)))))
    (is (thrown? #?(:cljs :default :clj Exception)
                 (into [] (drop-while nil) (range 0 10))))))
