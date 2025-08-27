(ns clojure.core-test.denominator
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/denominator
  (deftest test-denominator
    (is (= 2 (denominator 1/2)))
    (is (= 3 (denominator 2/3)))
    (is (= 4 (denominator 3/4)))

    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator 1)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator 1.0)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator 1N)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator 1.0M)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator ##Inf)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator ##NaN)))
    (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (denominator nil)))))
