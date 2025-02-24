(ns clojure.string-test.reverse
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/reverse
  (deftest test-reverse
    (is (= "" (str/reverse "")))
    (is (= "֎" (str/reverse "֎")))
    (is (= "a֎" (str/reverse "֎a")))
    (is (= "tset-a" (str/reverse "a-test")))
    (is (thrown? #?(:cljs :default :clj Exception) (str/reverse nil)))
    (is (thrown? #?(:cljs :default :clj Exception) (str/reverse 1)))
    (is (thrown? #?(:cljs :default :clj Exception) (str/reverse 'a-test)))
    (is (thrown? #?(:cljs :default :clj Exception) (str/reverse :a-test)))))
