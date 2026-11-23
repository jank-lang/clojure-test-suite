(ns clojure.core-test.reduce
  (:require
   [clojure.test :as t :refer [deftest is are testing]]
   [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p])
  #?(:clj (:import (clojure.lang IReduce))))

(when-var-exists reduce

  ;; Docstring:
  ;; clojure.core/reduce
  ;; ([f coll] [f val coll])
  ;; f should be a function of 2 arguments. If val is not supplied,
  ;; returns the result of applying f to the first 2 items in coll, then
  ;; applying f to that result and the 3rd item, etc. If coll contains no
  ;; items, f must accept no arguments as well, and reduce returns the
  ;; result of calling f with no arguments.  If coll has only 1 item, it
  ;; is returned and f is not called.  If val is supplied, returns the
  ;; result of applying f to val and the first item in coll, then
  ;; applying f to that result and the 2nd item, etc. If coll contains no
  ;; items, returns val and f is not called.

  ;; NOTE: we stick to testing `reduce` here, and specifically don't
  ;; test `reduced` as part of that. Since `reduced` is a separate
  ;; function, it has its own tests which will validate it.

  (deftest test-reduce
    ;; first, create a bunch of reducible collections
    (let [a-range (range 50)
          a-list (apply list a-range)
          a-vec (vec a-range)
          a-set (set a-range)
          an-obj-array (object-array a-range)
          an-int-array (int-array a-range)
          a-long-array (long-array a-range)
          #?@(:cljs []               ; CLJS doesn't have `float-array`
              :default [a-float-array (float-array a-range)])
          a-double-array (double-array a-range)
          #?@(:cljs []             ; CLJS doesn't have `boolean-array`
              :default [a-boolean-array (boolean-array (repeat 50 true))])]
      (testing "arity-2"
        ;; test various collection types
        (are [expected f coll] (= expected (reduce f coll))
          1225 + a-range
          1225 + a-list
          1225 + a-vec
          1225 + a-set
          1225 + an-obj-array
          1225 + an-int-array
          1225 + a-long-array
          #?@(:cljs []               ; CLJS doesn't have `float-array`
              :default [1225.0 + a-float-array])
          1225.0 + a-double-array
          #?@(:cljs []             ; CLJS doesn't have `boolean-array`
              :default [true #(and %1 %2) a-boolean-array])
          0 + '() ; calls arity-0 `f` for initial value, which is then returned
          0 + []
          0 + #{}
          0 + (int-array [])
          0 + nil)

        ;; Validate rules for generating initial element(s)
        ;; `coll` has three elements, so call arity-2 `f` with first
        ;; two, then with third, etc.
        (is (= "Hello, world"
               (reduce (fn
                         ([] (throw (ex-info "Not called" {})))
                         ([x y] (str x y))) ; called with first two elements, then third
                       ["Hello" ", " "world"])))
        ;; `coll` has exactly two elements, so call arity-2 `f` with first two and return result
        (is (= "Hello, world"
               (reduce (fn
                         ([] (throw (ex-info "Not called" {})))
                         ([x y] (str x y))) ; called with first two elements, then third
                       ["Hello, " "world"])))
        ;; `coll` has one element, so simply return it
        (is (= "Hello, world"
               (reduce (fn
                         ([] (throw (ex-info "Not called" {})))
                         ([x y] (throw (ex-info "Not called" {}))))
                       ["Hello, world"])))
        ;; `coll` is empty, so call arity-0 `f` and return the result
        (is (= "Hello, world"
               (reduce (fn
                         ([] "Hello, world") ; called because empty coll
                         ([x y] (throw (ex-info "Not called" {}))))
                       []))))

      (testing "arity-3"
        (are [expected f val coll] (= expected (reduce f val coll))
          1228 + 3 a-range
          1228 + 3 a-list
          1228 + 3 a-vec
          1228 + 3 a-set
          1228 + 3 an-obj-array
          1228 + 3 an-int-array
          1228 + 3 a-long-array
          #?@(:cljs []               ; CLJS doesn't have `float-array`
              :default [1228.0 + 3.0 a-float-array])
          1228.0 + 3 a-double-array
          #?@(:cljs []             ; CLJS doesn't have `boolean-array`
              :default [true #(and %1 %2) true a-boolean-array])
          [\h \e \l \l \o] conj [\h] "ello"
          ;; Basilisp does not currently implement sorted collections; sort a
          ;; plain map's entries to control traversal order there instead.
          #?@(:lpy [[[:a 1] [:b 2] [:c 3] [:d 4]] conj [] (sort-by first (seq {:a 1 :b 2 :c 3 :d 4}))]
              :default [[[:a 1] [:b 2] [:c 3] [:d 4]] conj [] (sorted-map :a 1 :b 2 :c 3 :d 4)])
          {1 :a, 2 :b, 3 :c, 4 :d} (fn [m [k v]] (conj m [v k])) {} {:a 1 :b 2 :c 3 :d 4}
          3 + 3 '()
          3 + 3 []
          3 + 3 #{}
          3 + 3 (int-array [])
          3 + 3 nil))

      (testing "negative tests"
        (is (p/thrown? (reduce nil nil))) ; tries to call arity-0 `f` to generate inital val
        (is (p/thrown? (reduce nil nil [1 2 3]))) ; `nil` is not a function
        (is (nil? (reduce nil nil nil))) ; `f` is never called, returns `val`
        (is (= :whatever (reduce nil :whatever nil)))
        (is (p/thrown? (reduce + 42)))
        (is (p/thrown? (reduce + :foo)))))))
