(ns clojure.core-test.not-every-qmark
  (:require [clojure.core-test.every-qmark :as every]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]
            [clojure.test :refer [deftest]]))

(when-var-exists not-every?
  (deftest test-not-every?
    (every/tests (complement not-every?))))
