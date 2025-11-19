(ns clojure.core-test.make-hierarchy
  (:require [clojure.test :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists make-hierarchy
  (deftest test-make-hierarchy
      (is (= {:parents {}, :descendants {}, :ancestors {}} (make-hierarchy)))))
