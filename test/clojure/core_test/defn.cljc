(ns clojure.core-test.defn
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defn

  ;; DOCSTRING:
  ;; clojure.core/defn
  ;; [name doc-string? attr-map? [params*] prepost-map? body]
  ;; [name doc-string? attr-map? ([params*] prepost-map? body) + attr-map?]
  ;; Same as (def name (fn [params* ] exprs*)) or (def
  ;; name (fn ([params* ] exprs*)+)) with any doc-string or attrs added
  ;; to the var metadata. prepost-map defines a map with optional keys
  ;; :pre and :post that contain collections of pre or post conditions.

  ;; `defn` is expanded at compile time and defines a var, so the
  ;; functions that exercise it have to be defined at the top level of
  ;; this namespace, outside of the `deftest` below.

  ;; [name doc-string? attr-map? [params*] prepost-map? body]
  (defn no-argument-fn [] :no-arguments)
  (defn one-argument-fn [x] (inc x))
  (defn variadic-fn [x & more] [x more])
  (defn empty-body-fn [x])

  (defn documented-fn
    "Returns its argument."
    [x]
    x)

  (defn attributed-fn
    "Returns its argument, with an attribute."
    {:custom-attr :custom-value}
    [x]
    x)

  ;; [name doc-string? attr-map? ([params*] prepost-map? body) + attr-map?]
  (defn multi-arity-fn
    ([] :no-arguments)
    ([x] [:one-argument x])
    ([x y] [:two-arguments x y])
    ([x y & more] [:variadic x y more]))

  ;; Note that in the multi-arity form the attr-map comes after the arities.
  (defn documented-multi-arity-fn
    "Adds its arguments."
    ([x] x)
    ([x y] (+ x y))
    {:custom-attr :custom-value})

  ;; The prepost-map supplies :pre conditions, checked on entry, and/or
  ;; :post conditions, checked on exit with % bound to the result.
  (defn pre-condition-fn
    [x]
    {:pre [(pos? x)]}
    (* 2 x))

  (defn post-condition-fn
    [x]
    {:post [(pos? %)]}
    (- x))

  (defn pre-and-post-condition-fn
    [x]
    {:pre [(pos? x)]
     :post [(even? %)]}
    (+ x 1))

  ;; In a multi-arity function a prepost-map belongs to a single arity.
  (defn multi-arity-prepost-fn
    ([x]
     {:pre [(pos? x)]}
     x)
    ([x y]
     {:post [(pos? %)]}
     (- x y)))

  (deftest test-defn
    (testing "same as (def name (fn [params*] body))"
      (is (= :no-arguments (no-argument-fn)))
      (is (= 2 (one-argument-fn 1)))
      (is (nil? (empty-body-fn :ignored)))
      (is (= [1 nil] (variadic-fn 1)))
      (is (= [1 [2 3]] (variadic-fn 1 2 3))))

    (testing "same as (def name (fn ([params*] body)+))"
      (is (= :no-arguments (multi-arity-fn)))
      (is (= [:one-argument 1] (multi-arity-fn 1)))
      (is (= [:two-arguments 1 2] (multi-arity-fn 1 2)))
      (is (= [:variadic 1 2 [3 4]] (multi-arity-fn 1 2 3 4))))

    (testing "doc-string is added to the var metadata"
      (is (= "Returns its argument." (:doc (meta #'documented-fn))))
      (is (= "Returns its argument, with an attribute." (:doc (meta #'attributed-fn))))
      (is (= "Adds its arguments." (:doc (meta #'documented-multi-arity-fn))))
      (is (nil? (:doc (meta #'one-argument-fn)))))

    (testing "attr-map is added to the var metadata"
      (is (= :custom-value (:custom-attr (meta #'attributed-fn))))
      (is (= :custom-value (:custom-attr (meta #'documented-multi-arity-fn))))
      (is (nil? (:custom-attr (meta #'documented-fn)))))

    (testing ":pre conditions are checked before the body runs"
      (is (= 4 (pre-condition-fn 2)))
      (is (= 1 (multi-arity-prepost-fn 1)))
      (is (p/thrown? (pre-condition-fn 0)))
      (is (p/thrown? (pre-condition-fn -1)))
      (is (p/thrown? (multi-arity-prepost-fn 0))))

    (testing ":post conditions are checked after the body runs, with % bound to the result"
      (is (= 1 (post-condition-fn -1)))
      (is (= 2 (multi-arity-prepost-fn 4 2)))
      (is (p/thrown? (post-condition-fn 1)))
      (is (p/thrown? (multi-arity-prepost-fn 4 5))))

    (testing ":pre and :post conditions together"
      (is (= 2 (pre-and-post-condition-fn 1)))
      (is (p/thrown? (pre-and-post-condition-fn 0)))   ; fails the :pre condition
      (is (p/thrown? (pre-and-post-condition-fn 2)))))) ; fails the :post condition
