(ns clojure.core-test.nth
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/nth
  (deftest test-nth
    (is (= 0 (nth (range 0 10) 0)))
    (is (= 5 (nth (range 0 10) 5)))
    (is (= 5 (nth [0 1 2 3 4 5 6 7 8 9] 5)))
    (is (= 5 (nth (range) 5)))          ; infinite lazy range

    ; Unexpected. `nil` not treated like `(), which would throw
    (is (nil? (nth nil 10)))

    ;; `nth` throws if out of range
    (is (thrown? #?(:cljs :default, :clj Exception :cljr Exception) (nth [0 1 2] -1)))
    (is (thrown? #?(:cljs :default, :clj Exception :cljr Exception) (nth [0 1 2] 10)))
    (is (thrown? #?(:cljs :default, :clj Exception :cljr Exception) (nth [0 1 2] nil)))
    (is (thrown? #?(:cljs :default, :clj Exception :cljr Exception) (nth nil nil)))

    ;; `nth` accepts a default argument
    (is (= :default (nth nil 0 :default)))
    (is (= :default (nth [0] 1 :default)))
    (is (= :default (nth [0 1] 2 :default)))
    (is (= :default (nth [0 1] -1 :default)))))
