(ns clojure.core-test.class
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/class

  ;; DOCSTRING:
  ;; clojure.core/class
  ;; [x]
  ;; Returns the Class of x

  ;; A record and a type, to exercise classes of user-defined types.
  (defrecord TestClassRecord [x])
  (deftype TestClassType [x])

  (deftest test-class
    (testing "nil has no class"
      (is (nil? (class nil))))

    (testing "every other value is an instance of its class"
      (are [x] (instance? (class x) x)
        true
        false
        0
        1.5
        1N
        1.0M
        #?@(:cljs []
            :default
            [1/2])
        \a
        "a string"
        :keyword
        'symbol
        []
        '()
        {}
        #{}
        (range)
        (seq [1 2])
        (map inc [1 2])
        (atom 1)
        (->TestClassRecord 1)
        (->TestClassType 1))

      ;; A function literal denotes a fresh function each time it is
      ;; evaluated, so bind one before applying `class` to it.
      (let [f (fn [])]
        (is (instance? (class f) f))))

    (testing "values of the same type have the same class"
      (are [a b] (= (class a) (class b))
        []                    [1 2 3]
        {}                    {:a 1}
        #{}                   #{1 2}
        '(1)                  '(1 2 3)
        ""                    "a string"
        :keyword              :another-keyword
        'symbol               'another-symbol
        0                     -1
        1.5                   ##NaN
        (atom 1)              (atom "another value")
        (seq [1])             (seq [2 3])
        (map inc [1])         (map inc [2 3])
        (->TestClassRecord 1) (->TestClassRecord 2))

      (testing "metadata does not affect the class"
        (are [x] (= (class x) (class (with-meta x {:a 1})))
          []
          [1 2]
          '(1)
          {:a 1}
          #{1 2})))

    (testing "values of different types have different classes"
      (are [a b] (not= (class a) (class b))
        []                    '()
        {}                    #{}
        :keyword              'symbol
        \a                    "a"
        "1"                   1
        0                     1.5
        0                     0N
        0                     0.0M
        (atom 1)              []
        (->TestClassRecord 1) (->TestClassType 1)))

    (testing "values that are equal may still have different classes"
      (are [a b] (and (= a b) (not= (class a) (class b)))
        0       0N
        [1 2]   (seq [1 2])))

    (testing "a class is itself a value, whose class is shared by all classes"
      (are [a b] (= (class (class a)) (class (class b)))
        0        "a string"
        (atom 1) [#{1 2}])
      ;; As with any other value, a class is an instance of its own class.
      (is (instance? (class (class 0)) (class 0))))

    (testing "the argument is not realized"
      (let [realized (atom false)
            xs       (map (fn [x] (reset! realized true) x) (range))]
        (is (some? (class xs)))
        (is (false? @realized))))))
