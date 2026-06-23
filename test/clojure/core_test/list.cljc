(ns clojure.core-test.list
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]
            [clojure.test :refer [are deftest is testing]]))

(when-var-exists list
  (deftest test-list
    (testing "satisfies list?"
      (are [v] (list? v)
        (list)
        (list 1)
        (list 1 2)
        (list 1 2 3)))

    (testing "empty list"
      (is (= '() (list))))

    (testing "single element"
      (are [expected v] (= expected (list v))
        '(1) 1
        '(:a) :a
        '("string") "string"
        '(nil) nil
        '(\a) \a
        '(true) true
        '(false) false))

    (testing "multiple elements"
      (are [expected actual] (= expected actual)
        '(1 2 3) (list 1 2 3)
        '(:a :b :c) (list :a :b :c)
        '("zz" "a" "42") (list "zz" "a" "42")))

    (testing "multiple data structures"
      (are [expected actual] (= expected actual)
        '([1 2] [3 4]) (list [1 2] [3 4])
        '((1 2) (3 4)) (list '(1 2) '(3 4))
        '({:a 1} {:b 2}) (list {:a 1} {:b 2})
        '(#{1 2} #{3 4}) (list #{1 2} #{3 4})
        '([]) (list [])
        '(()) (list '())
        '({}) (list {})
        '(#{}) (list #{})))

    (testing "preserves duplicates"
      (are [expected actual] (= expected actual)
        '(1 1 1) (list 1 1 1)
        '(:a :a) (list :a :a)
        '(nil nil nil) (list nil nil nil)))

    (testing "large lists"
      (let [result (apply list (range 100))]
        (is (= (range 100) result))
        (is (list? result))))

    (testing "different types together"
      (are [expected actual] (= expected actual)
        '(1 2.5 3N 4M) (list 1 2.5 3N 4M)
        '(1 :a "b" \c true nil () [] #{} {}) (list 1 :a "b" \c true nil '() [] #{} {})))

    (testing "variadic args"
      (are [expected actual] (= expected actual)
        '(1 2 3 4 5 6 7 8 9 10) (list 1 2 3 4 5 6 7 8 9 10)
        '(1 2 3 4 5 6 nil) (list 1 2 3 4 5 6 nil)))))
