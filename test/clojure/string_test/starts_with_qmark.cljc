(ns clojure.string-test.starts-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/starts-with?
  (deftest test-starts-with?
    (is (true? (str/starts-with? "" "")))
    #?(:cljs (is (false? (str/starts-with? "" nil)))
       :default (is (thrown? #?(:clj Exception) (str/starts-with? "" nil))))
    #?(:cljs (do (is (true? (str/starts-with? "ab" :a)))
                 (is (false? (str/starts-with? "ab" :b))))
       :default (is (thrown? #?(:clj Exception) (str/starts-with? "ab" :a))))
    #?(:cljs (is (true? (str/starts-with? "ab" 'a)))
       :default (is (thrown? #?(:clj Exception) (str/starts-with? "a" 'a))))
    #?(:cljs (do (is (false? (str/starts-with? 'ab "b")))
                 (is (true? (str/starts-with? 'ab "a"))))
       :default (is (thrown? #?(:clj Exception) (str/starts-with? 'ab "b"))))
    #?(:cljs (do (is (false? (str/starts-with? :ab "b")))
                 (is (true? (str/starts-with? :ab "a"))))
       :default (is (thrown? #?(:clj Exception) (str/starts-with? :ab "b"))))
    (is (false? (str/starts-with? "" "a")))
    (is (true? (str/starts-with? "a-test" "")))
    (is (true? (str/starts-with? "a-test" "a")))
    (is (true? (str/starts-with? "a-test" "a-test")))
    (is (false? (str/starts-with? "a-test" "-")))
    (is (false? (str/starts-with? "a-test" "t"))))
