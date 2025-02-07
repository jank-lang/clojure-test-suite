(ns clojure.core-test.take-last
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/take-last
  (deftest test-take-last
    (is (= (range 8 10) (take-last 2 (range 0 10))))
    (is (nil? (take-last 2 nil)))       ; Returns `nil`, not `()`

    ;; Note that there is no transducer version of `take-last` in
    ;; `clojure.core`.

    ;; Negative testing
    (is (thrown? #?(:cljs :default :clj Exception)
                 (doall (take-last nil (range 0 10)))))))
