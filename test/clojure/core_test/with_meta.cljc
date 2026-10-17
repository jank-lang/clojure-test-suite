(ns clojure.core-test.with-meta
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists with-meta
  (deftest test-with_meta
    (testing "adding a map as metadata to another value"
      (are [expected x m] (= expected (meta (with-meta x m)))
        nil {} nil
        {:a 0} {} {:a 0}
        {:a 0} ^:b {} {:a 0} ;; with-meta overrides existing metadata
        ))

    (testing "exception cases"
      #?(:cljs (is (= {} (with-meta {} :not-a-map)))
         :default (is (p/thrown? (with-meta {} :not-a-map))))
      )))
