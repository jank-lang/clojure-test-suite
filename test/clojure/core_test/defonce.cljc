(ns clojure.core-test.defonce
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists defonce
  (deftest test-defonce

    #?(:cljs    "shadow-cljs treats defonce like def (https://github.com/thheller/shadow-cljs/issues/1185)"
       :default (testing "defining var only once"
                  (is (some? (defonce one 1)))
                  (is (nil? (defonce one 2)))
                  (is (nil? (defonce one (assert false "should not be evaluated"))))
                  (is (= 1 one))))

    (testing "bad shape"
      (are [name] (thrown? #?(:cljs js/Error :default Exception) (eval '(defonce name nil)))
                  nil
                  :k
                  's
                  0
                  37.5
                  true
                  false
                  '()
                  []
                  #{}
                  {}))))
