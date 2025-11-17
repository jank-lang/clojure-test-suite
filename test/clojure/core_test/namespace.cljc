(ns clojure.core-test.namespace
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists namespace
 (deftest test-namespace
   (are [expected sym-or-kw] (= expected (namespace sym-or-kw))
     "clojure.core" 'clojure.core/+
     "abc"          :abc/def
     "abc"          'abc/def
     nil            :abc
     nil            'abc)

   (is (thrown? #?(:cljs :default :default Exception) (namespace nil)))))
