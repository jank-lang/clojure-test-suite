(ns clojure.core-test.pop-bang
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists pop!
  (deftest test-pop!

    (testing "nominal cases"
      (are [expected vec] (= expected (persistent! (pop! (transient vec))))
                          [] [nil]
                          [] [1]
                          [1 2] [1 2 3]
                          [:c :b] [:c :b :a]))

    (testing "cannot pop! empty vector"
      (is (thrown? #?(:cljs js/Error :default Exception) (pop! (transient [])))))

    (testing "cannot pop! after call to persistent!"
      (let [t (transient [0 1]), _ (persistent! t)]
        (is (thrown? #?(:cljs js/Error :cljr Exception :default Error) (pop! t)))))

    (testing "bad shapes"
      (are [arg] (thrown? #?(:cljs js/Error :default Exception) (pop! arg))
                 (transient {:a 0})
                 (transient #{0})
                 [0]
                 '(0)
                 #{0}
                 (range 3)
                 true
                 false
                 "s"
                 3.14
                 42
                 nil))))
