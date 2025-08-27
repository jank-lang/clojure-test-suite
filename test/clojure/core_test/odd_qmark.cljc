(ns clojure.core-test.odd-qmark
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/odd?
  (deftest test-odd?
    (testing "common"
      (are [in ex] (= (odd? in) ex)
        0     false
        -0    false
        12    false
        17    true
        -118  false
        -119  true
        123N  true
        122N  false
        -121N true
        -120N false))

    (testing "invalid"
      (are [x] (thrown? #?(:clj Exception  :cljr Exception :cljs js/Error) (odd? x))
        #_:clj-kondo/ignore nil
        ##Inf
        ##-Inf
        ##NaN
        1.5
        #?@(:cljs []
            :default
            [1/2])
        0.2M))))
