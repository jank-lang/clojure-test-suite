(ns clojure.core-test.empty
  (:require clojure.core
            [clojure.core-test.number-range :as r]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/empty
  (deftest test-empty
    (testing "common"
      (are [expected x] (= expected (empty x))
           []  [1 2]
           {}  {:a :map}
           '() '(1 2)
           '() (range)
           '() (range 10)
           #{} #{1 2 3}
           nil ""
           nil \space
           nil :a
           nil 1
           nil 0.0
           nil nil
           nil map
           nil r/max-int
           nil r/min-int
           #?@(:cljs [nil (js/Date)]
		       :cljr [nil (new Object)]
               :clj  [nil (new Object)])))

    (when-var-exists clojure.core/defrecord
      (testing "record"
        (defrecord Record [field])
        #?@(:cljs [(is (= nil (empty (->Record ""))))]
		    :cljr  [(is (thrown? InvalidOperationException (empty (->Record ""))))]
            :clj  [(is (thrown? UnsupportedOperationException (empty (->Record ""))))])))

    (when-var-exists clojure.core/deftype
      (testing "datatype"
        (deftype MyType [field])
        (is (= nil (empty (->MyType ""))))))))

