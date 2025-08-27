(ns clojure.core-test.namespace
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/namespace
 (deftest test-namespace
   (are [expected sym-or-kw] (= expected (namespace sym-or-kw))
     "clojure.core" 'clojure.core/+
     "abc"          :abc/def
     "abc"          'abc/def
     nil            :abc
     nil            'abc)

   (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (namespace nil)))))
