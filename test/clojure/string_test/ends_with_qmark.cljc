(ns clojure.string-test.ends-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/ends-with?
  (deftest test-ends-with?
    (is (true? (str/ends-with? "" "")))
    #?(:cljs (is (false? (str/ends-with? "" nil)))
       :default (is (thrown? #?(:clj Exception) (str/ends-with? "" nil))))
    (is (false? (str/ends-with? "" "a")))
    (is (true? (str/ends-with? "a-test" "t")))
    (is (false? (str/ends-with? "a-test" "s")))))
