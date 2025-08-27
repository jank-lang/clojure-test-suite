(ns clojure.core-test.take
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/take
  (deftest test-take
    (is (= (range 0 5) (take 5 (range 0 10))))
    (is (= (range 0 5) (take 5 (range)))) ; Infinite `range` lazy seq
    (is (= '() (take 5 nil)))
    
    ;; transducer versions
    (is (= (vec (range 0 5)) (into [] (take 5) (range 0 10))))
    (is (= (vec (range 0 5)) (into [] (take 5) (range))))
    (is (= [] (into [] (take 5) nil)))

    ;; negative tests
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                 (doall (take nil (range 0 10)))))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                 (into [] (take nil) (range 0 10))))))
