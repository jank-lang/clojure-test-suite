(ns clojure.core-test.declare
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/declare

  ;; DOCSTRING:
  ;; clojure.core/declare
  ;; [& names]
  ;; defs the supplied var names with no bindings, useful for making forward declarations.

  ;; `declare` defs vars, so the vars it creates have to be made at the top
  ;; level of this namespace. `when-var-exists` expands to a top-level `do`,
  ;; so these declarations are top-level and can live here rather than
  ;; outside the guard.

  ;; A declared var that never gets a binding.
  (declare never-bound)

  ;; Several names may be declared at once.
  (declare multi-a multi-b multi-c)

  ;; The body of `call-forward-declared` is compiled before
  ;; `forward-declared-fn` exists; the declaration below is what allows the
  ;; compiler to resolve it.
  (declare forward-declared-fn)
  (defn call-forward-declared [x] (forward-declared-fn x))

  ;; Declaring a var that is already defined does not disturb its binding.
  (def already-defined :value)
  (declare already-defined)

  ;; Now supply the binding the declaration promised.
  (defn forward-declared-fn [x] (inc x))

  (deftest test-declare
    (testing "defs the supplied var names"
      (is (var? #'never-bound))
      (is (= 'never-bound (:name (meta #'never-bound)))))

    (testing "several names may be declared at once"
      (are [v] (var? v)
        #'multi-a
        #'multi-b
        #'multi-c))

    (testing "the vars are declared with no bindings"
      ;; An unbound var differs between implementations: deref yields an
      ;; unbound sentinel in Clojure JVM, Babashka and ClojureCLR, and nil
      ;; in ClojureScript. The absence of a binding is what is checked.
      #?(:cljs (is (nil? (deref #'never-bound)))
         :default (is (not (bound? #'never-bound))))
      ;; Calling an unbound var fails.
      (is (p/thrown? (never-bound)))
      (is (p/thrown? (apply never-bound []))))

    (testing "a forward declaration makes a later definition callable"
      ;; `call-forward-declared` was compiled against the declaration only.
      (is (= 42 (call-forward-declared 41))))

    (testing "declaring an already-defined var keeps its binding"
      (is (= :value already-defined)))

    (testing "declaring no names is allowed"
      (is (nil? (declare))))))
