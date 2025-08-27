(ns clojure.core-test.drop-last
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/drop-last
  (deftest test-drop-last
    ;; drop the last item
    (= (range 0 9) (drop-last (range 0 10)))
    (= '() (drop-last nil))
    ;; drop the last n items
    (= (range 0 5) (drop-last 5 (range 0 10)))
    (= '() (drop-last 5 nil))
    

    ;; Negative tests
    ;; Note: `doall` is required to realize the lazy sequence and
    ;; force it to throw
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                 (doall (drop-last nil (range 5)))))))
