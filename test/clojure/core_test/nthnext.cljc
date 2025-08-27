(ns clojure.core-test.nthnext
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/nthnext
  (deftest test-nthnext
    (is (= '(3 4 5 6 7 8 9) (nthnext (range 0 10) 3)))
    (is (= [3 4 5] (nthnext [0 1 2 3 4 5] 3)))
    (is (= (range 0 10) (nthnext (range 0 10) 0)))
    (is (= '(9) (nthnext (range 0 10) 9)))
    (is (nil? (nthnext (range 0 10) 10)))
    (is (nil? (nthnext (range 0 10) 100)))
    (is (nil? (nthnext [1 2 3] 100)))
    (is (nil? (nthnext nil 100)))
    (is (nil? (nthnext [] 100)))
    (is (= (range 3) (nthnext (range 3) -1)))

    (is (nil? (nthnext nil nil)))       ; Surprising. Should perhaps throw.
    
    ;; Negative tests
    #?@(:cljs
        ;; CLJS does some nil punning to 0
        [(is (= (range 0 10) (nthnext (range 0 10) nil)))
         (is (= '(0 1 2) (nthnext [0 1 2] nil)))]
        :default
        [(is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (nthnext (range 0 10) nil)))
         (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (nthnext [0 1 2] nil)))])))
