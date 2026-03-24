(ns clojure.string-test.ends-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists str/ends-with?
  (deftest test-ends-with?
    (is (true? (str/ends-with? "" "")))
    (is (p/thrown? (str/ends-with? "" nil)))

   #?(:cljs (is (p/thrown? (str/ends-with? nil "")))
      :cljr (is (true? (str/ends-with? nil "")))
      :default (is (p/thrown? (str/ends-with? nil ""))))

    #?(:cljs (do (is (false? (str/ends-with? "ab" :b)))
                 (is (false? (str/ends-with? "ab" :a))))
       :default (is (p/thrown? (str/ends-with? "ab" :b))))

    #?(:cljs (is (false? (str/ends-with? "ab" 'b)))
       :default (is (p/thrown? (str/ends-with? "ab" 'b))))

   #?@(:cljs
       [(is (false? (str/ends-with? 'ab "b")))
        (is (false? (str/ends-with? 'ab "a")))
        (is (false? (str/ends-with? :ab "b")))
        (is (false? (str/ends-with? :ab "a")))]
       :cljr
       [(is (p/thrown? (str/ends-with? 'ab "b")))
        (is (p/thrown? (str/ends-with? 'ab "a")))
        (is (p/thrown? (str/ends-with? :ab "b")))
        (is (p/thrown? (str/ends-with? :ab "b")))]
       :lpy
       [(is (p/thrown? (str/ends-with? 'ab "b")))
        (is (p/thrown? (str/ends-with? 'ab "a")))
        (is (p/thrown? (str/ends-with? :ab "b")))
        (is (p/thrown? (str/ends-with? :ab "b")))]
       :default
       [(is (true? (str/ends-with? 'ab "b")))
        (is (false? (str/ends-with? 'ab "a")))
        (is (true? (str/ends-with? :ab "b")))
        (is (false? (str/ends-with? :ab "a")))])

    (is (false? (str/ends-with? "" "a")))
    (is (true? (str/ends-with? "a-test" "")))
    (is (true? (str/ends-with? "a-test֎" "֎")))
    (is (true? (str/ends-with? "a-test" "t")))
    (is (true? (str/ends-with? "a-test" "a-test")))
    (is (false? (str/ends-with? "a-test" "s")))
    (is (false? (str/ends-with? "a-test" "a")))))
