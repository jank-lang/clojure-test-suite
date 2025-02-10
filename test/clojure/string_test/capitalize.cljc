(ns clojure.string-test.capitalize
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/capitalize
  (deftest test-capitalize
    (is (thrown? #?(:cljs :default :clj Exception) (str/capitalize nil)))
    #?(:cljs nil
       :default (do (is (= "1" (str/capitalize 1)))
                    (is (= "Asdf" (str/capitalize 'Asdf)))
                    (is (= "Asdf" (str/capitalize 'Asdf)))
                    (is (= ":clojure.string-test.capitalize/asdf" (str/capitalize ::asdf)))
                    (is (= ":clojure.string-test.capitalize/asdf" (str/capitalize ::Asdf)))))
    (is (= "" (str/capitalize "")))
    (is (= "A" (str/capitalize "a")))
    (is (= "A thing" (str/capitalize "a Thing")))
    (is (= "A thing" (str/capitalize "A THING")))
    (is (= "A thing" (str/capitalize "A thing")))))
