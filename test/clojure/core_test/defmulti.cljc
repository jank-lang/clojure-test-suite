(ns clojure.core-test.defmulti
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defmulti

  ;; DOCSTRING:
  ;; clojure.core/defmulti
  ;; [name docstring? attr-map? dispatch-fn & options]
  ;; Creates a new multimethod with the associated dispatch function.
  ;; The docstring and attr-map are optional.
  ;; 
  ;; Options are key-value pairs and may be one of:
  ;; 
  ;; :default
  ;; 
  ;; The default dispatch value, defaults to :default
  ;; 
  ;; :hierarchy
  ;; 
  ;; The value used for hierarchical dispatch (e.g. ::square is-a ::shape)
  ;; 
  ;; Hierarchies are type-like relationships that do not depend upon type
  ;; inheritance. By default Clojure's multimethods dispatch off of a
  ;; global hierarchy map.  However, a hierarchy relationship can be
  ;; created with the derive function used to augment the root ancestor
  ;; created with make-hierarchy.
  ;; 
  ;; Multimethods expect the value of the hierarchy option to be supplied as
  ;; a reference type e.g. a var (i.e. via the Var-quote dispatch macro #'
  ;; or the var special form).

  ;; `defmulti` is a macro that defines a multimethod, so each multimethod
  ;; these tests exercise is defined here, at the top level, rather than
  ;; inside the `deftest`. Installing and invoking methods is covered by the
  ;; `defmethod` tests; these multimethods get only the methods that the
  ;; tests of `defmulti` itself need.

  ;; The dispatch function may take any number of arguments, and the
  ;; multimethod dispatches on the value that it returns.
  (defmulti by-both-arguments (fn [a b] [a b]))
  (defmethod by-both-arguments [:a :b] [_ _] :both-arguments-method)

  ;; The docstring and the attr-map are optional.
  (defmulti with-docstring-and-attr-map "a docstring." {:custom :attr} identity)

  ;; The :default option replaces the default dispatch value.
  (defmulti with-custom-default identity :default ::custom-default)
  (defmethod with-custom-default ::custom-default [_] :custom-default-method)

  ;; The :hierarchy option supplies a hierarchy as a reference type.
  (def shapes-hierarchy (derive (make-hierarchy) ::square ::shape))
  (defmulti by-shape (fn [m] (:shape m)) :hierarchy #'shapes-hierarchy)
  (defmethod by-shape ::shape [_] :shape-method)
  (defmethod by-shape :default [_] :default-method)

  ;; The same dispatch, but with the default (global) hierarchy.
  (defmulti by-shape-globally (fn [m] (:shape m)))
  (defmethod by-shape-globally :default [_] :default-method)

  (deftest test-defmulti
    (testing "the multimethod dispatches on the value returned by the dispatch function"
      ;; The dispatch function receives all of the arguments of the call.
      (is (= :both-arguments-method (by-both-arguments :a :b))))

    (testing "the docstring and the attr-map are optional"
      (is (= "a docstring." (:doc (meta #'with-docstring-and-attr-map))))
      (is (= :attr (:custom (meta #'with-docstring-and-attr-map)))))

    (testing "the default dispatch value is :default unless the :default option is given"
      ;; ::square is-a ::shape in shapes-hierarchy, but ::rect is not in it.
      (is (= :default-method (by-shape {:shape ::rect})))
      (is (= :custom-default-method (with-custom-default :a-keyword-with-no-method))))

    (testing "the :hierarchy option supplies the hierarchy that dispatch uses"
      (is (= :shape-method (by-shape {:shape ::square})))
      ;; The global hierarchy does not derive ::square from ::shape.
      (is (= :default-method (by-shape-globally {:shape ::square}))))))
