(ns clojure.core-test.val
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists val
  (deftest test-val
    (testing "`val` on map-entry-like things"
      (is (= :v (val (first {:k :v}))))
      (is (= :v (val (first {:k :v, :one :two}))))
      ;; Note: the following may be built on shaky ground, per Rich:
      ;; https://groups.google.com/g/clojure/c/FVcrbHJpCW4/m/Fh7NsX_Yb7sJ
      (is (= 'v (val #?(:cljs (cljs.core/MapEntry. 'k 'v nil)
                        :clj (clojure.lang.MapEntry/create 'k 'v)))))
      (is (= :b (val (first (sorted-map :a :b)))))
      (is (= :b (val (first (hash-map :a :b)))))
      (is (= :b (val (first (array-map :a :b))))))
    (testing "`val` throws on lots of things"
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val nil)))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val 0)))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val '())))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val '(1 2))))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val {})))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val {1 2})))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val [])))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val [1 2])))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val #{})))
      (is (thrown? #?(:cljs :default, :clj Exception, :cljr Exception)
                   (val #{1 2}))))))
