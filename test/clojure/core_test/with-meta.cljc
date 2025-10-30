(ns clojure.core-test.with-meta
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/first
  (deftest test-first
    (let* [meta-data {:foo 42}]
      (is (= meta-data (meta (empty (with-meta [] meta-data)))))
      (is (= meta-data (meta (empty (with-meta {} meta-data)))))
      (is (= meta-data (meta (empty (with-meta #{} meta-data)))))
      (is (= meta-data (meta (empty (with-meta '() meta-data))))))))

