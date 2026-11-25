(ns clojure.core-test.type
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/type

  ;; DOCSTRING:
  ;; clojure.core/type
  ;; [x]
  ;; Returns the :type metadata of x, or its Class if none

  (deftest test-type
    (testing "nil has no type"
      (is (nil? (type nil))))

    (testing "with no :type metadata, the type of the value is its Class"
      ;; ClojureScript has no `class`, so the comparison against the Class can
      ;; only be made in the other dialects. The `instance?` check below
      ;; validates the same property portably.
      #?(:cljs nil
         :default
         (are [x] (= (class x) (type x))
           true
           42
           -1.5
           "foo"
           \a
           :foo
           'foo
           [] [1 2 3]
           {} {:a 1}
           #{} #{:a}
           (list) (list 1 2 3)
           (seq [1 2 3])
           (range 3)
           (lazy-seq nil)))

      ;; the type is a type that the value belongs to. This cannot be checked
      ;; for every value: in ClojureScript `type` returns the constructor (e.g.
      ;; String, Number), which `instance?` cannot test against.
      (are [x] (instance? (type x) x)
        []
        {}
        #{}
        (list 1 2)
        :foo
        (range 3)
        (lazy-seq nil))

      ;; a function object has no metadata either, so its Class is returned; the
      ;; function must be bound to a name, since on the JVM each `fn` form
      ;; compiles to its own distinct class
      (let [f (fn [x] x)]
        #?(:cljs (is (instance? (type f) f))
           :default (is (= (class f) (type f))))))

    ;; ClojureScript's `type` returns the constructor and never consults the
    ;; `:type` metadata (its docstring is "Return x's constructor", and
    ;; `with-meta` on a function produces a `MetaFn`), so the metadata contract
    ;; is validated separately in each dialect.
    #?@(:cljs
        [(testing "ClojureScript ignores :type metadata"
           (are [x] (= (type x) (type (with-meta x {:type :a-type})))
             [] {}
             #{} (list 1 2)
             'foo
             (range 3)
             (lazy-seq nil)))]

        :default
        [(testing ":type metadata is returned instead of the Class"
           (are [x] (= :a-type (type (with-meta x {:type :a-type})))
             [] {}
             #{} (list 1 2)
             'foo
             (range 3)
             (lazy-seq nil)
             ;; a function object carries metadata too
             (fn [x] x))

           ;; metadata attached by the reader is seen the same way
           (is (= :a-type (type ^{:type :a-type} [1 2 3]))))

         (testing "the :type metadata value is returned as is, whatever it is"
           (are [x typ] (= typ (type (with-meta x {:type typ})))
             [] "a-string-type"
             {} 'a-symbol-type
             #{} 42
             (list) {:a 1}))])

    (testing "metadata without a :type entry does not change the result"
      (are [x] (= (type x) (type (with-meta x {:other :value})))
        [] (list 1 2) 'foo))

    (testing "a nil :type metadata value falls back to the Class"
      (is (= (type []) (type (with-meta [] {:type nil})))))

    (testing "values of the same kind have the same type"
      (are [x y] (identical? (type x) (type y))
        [] [1 2 3]
        {} {:a 1}
        #{} #{:a :b}
        (list 1 2) (list 3 4)
        "" "foo"
        :foo :bar
        'foo (symbol "bar")
        (range 3) (range 10)
        (lazy-seq nil) (lazy-seq [1 2])))

    (testing "values of different kinds have different types"
      (are [x y] (not (identical? (type x) (type y)))
        [] {}
        [] #{}
        [] (list)
        {} #{}
        [] "foo"
        :foo 'foo))

    (testing "the types of different values are themselves of one type"
      (is (identical? (type (type [])) (type (type {}))))
      (is (identical? (type (type "foo")) (type (type :foo)))))))
