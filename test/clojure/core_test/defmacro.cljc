(ns clojure.core-test.defmacro
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]])
  ;; A ClojureScript macro is defined and consumed in different compilation
  ;; stages: it is written in a .cljc (or .clj) file, compiled as Clojure and
  ;; then used by the ClojureScript compiler. A .cljc namespace may supply the
  ;; macros for its own ClojureScript code, which is what the :require-macros
  ;; below does: the macros are defined once, at the top level below.
  #?@(:cljs [(:require-macros [clojure.core-test.defmacro
                               :refer [no-argument-macro
                                       one-argument-macro
                                       two-argument-macro
                                       variadic-macro
                                       empty-body-macro
                                       destructured-macro
                                       multi-arity-macro
                                       documented-macro
                                       attributed-macro
                                       documented-multi-arity-macro
                                       quote-argument-macro
                                       form-macro
                                       env-macro
                                       auto-gensym-macro]])]))

;; `defmacro` is expanded when it is compiled and defines a var, so the macros
;; that the tests below exercise are defined at the top level of this
;; namespace, outside of the `deftest`.

;; [name doc-string? attr-map? [params*] body]
(defmacro no-argument-macro [] :no-arguments)

(defmacro one-argument-macro [x] (list 'inc x))

(defmacro two-argument-macro [x y] (list 'vector x y))

;; `more` is the sequence of the remaining argument forms.
(defmacro variadic-macro [x & more] (list* 'vector x more))

(defmacro empty-body-macro [x])

(defmacro destructured-macro [{:keys [a b]} [c d]]
  (list 'vector a b c d))

;; [name doc-string? attr-map? ([params*] body) + attr-map?]
(defmacro multi-arity-macro
  ([] :no-arguments)
  ([x] [:one-argument x])
  ([x y] [:two-arguments x y])
  ([x y & more] [:variadic x y (vec more)]))

(defmacro documented-macro
  "Expands to its argument."
  [x]
  x)

(defmacro attributed-macro
  "Expands to its argument, with an attribute."
  {:custom-attr :custom-value}
  [x]
  x)

;; In the multi-arity form the attr-map comes after the arities.
(defmacro documented-multi-arity-macro
  "Expands to its argument, or to the sum of its arguments."
  ([x] x)
  ([x y] (list '+ x y))
  {:custom-attr :custom-value})

;; The argument of a macro is the unevaluated form, not its value.
(defmacro quote-argument-macro [x] (list 'quote x))

;; The implicit `&form` and `&env` parameters are the first two parameters of
;; every macro's function; they are not declared in the parameter vector.
;; `form-macro` expands to its own call form.
(defmacro form-macro [& args] (list 'quote &form))

;; `env-macro` expands to the names of the locals in scope at its call site.
;; Only the names are quoted: the values in `&env` are the compiler's own
;; local-binding objects, which cannot be embedded in an expansion.
;; In Clojure `&env` maps the local's symbol to its binding; in ClojureScript
;; `&env` is the compiler's environment map, which holds the locals under
;; `:locals`.
(defmacro env-macro []
  (list 'quote (set (keys (or (:locals &env) &env)))))

;; An autogensym (`x#`) is replaced by a fresh symbol when the macro is
;; expanded, so the expansion cannot capture the caller's `x`.
(defmacro auto-gensym-macro [] `(let [x# :internal] x#))

(when-var-exists clojure.core/defmacro

  ;; DOCSTRING:
  ;; clojure.core/defmacro
  ;; [name doc-string? attr-map? [params*] body]
  ;; [name doc-string? attr-map? ([params*] body) + attr-map?]
  ;; Like defn, but the resulting function name is declared as a
  ;; macro and will be used as a macro by the compiler when it is
  ;; called.

  (deftest test-defmacro
    (testing "the macro is used when it is called, and its body supplies the expansion"
      (is (= :no-arguments (no-argument-macro)))
      (is (= 42 (one-argument-macro 41)))
      (is (= [1 2] (two-argument-macro 1 2)))
      (is (nil? (empty-body-macro :ignored)))
      ;; The expansion is code, so the caller's forms are evaluated as part of
      ;; it rather than before the macro is called.
      (is (= 4 (one-argument-macro (+ 1 2)))))

    (testing "the arguments of a macro are the unevaluated forms"
      (is (= '(+ 1 2) (quote-argument-macro (+ 1 2))))
      (is (= '(/ 1 0) (quote-argument-macro (/ 1 0)))))

    (testing "the parameters are destructured as they are in defn"
      (is (= [1 2 3 4] (destructured-macro {:a 1 :b 2} [3 4]))))

    (testing "a variadic macro"
      (are [expected form] (= expected form)
        [1] (variadic-macro 1)
        [1 2] (variadic-macro 1 2)
        [1 2 3] (variadic-macro 1 2 3)))

    (testing "the ([params*] body) + arity declares one macro with several arities"
      (are [expected form] (= expected form)
        :no-arguments (multi-arity-macro)
        [:one-argument 1] (multi-arity-macro 1)
        [:two-arguments 1 2] (multi-arity-macro 1 2)
        [:variadic 1 2 [3 4]] (multi-arity-macro 1 2 3 4)))

    ;; A macro's var is a compile-time entity, so the metadata of the macro is
    ;; not available at runtime in ClojureScript.
    #?(:cljs nil
       :default
       (testing "the resulting function name is declared as a macro"
         (is (true? (:macro (meta #'no-argument-macro))))
         (is (true? (:macro (meta #'multi-arity-macro))))
         (is (true? (:macro (meta #'attributed-macro))))))

    #?(:cljs nil
       :default
       (testing "doc-string and attr-map are added to the var metadata"
         (is (= "Expands to its argument." (:doc (meta #'documented-macro))))
         (is (= "Expands to its argument, with an attribute."
                (:doc (meta #'attributed-macro))))
         (is (= "Expands to its argument, or to the sum of its arguments."
                (:doc (meta #'documented-multi-arity-macro))))
         (is (= :custom-value (:custom-attr (meta #'attributed-macro))))
         (is (= :custom-value (:custom-attr (meta #'documented-multi-arity-macro))))
         (is (nil? (:custom-attr (meta #'documented-macro))))))

    (testing "&form is bound to the call form, including its arguments"
      (is (= '(form-macro) (form-macro)))
      (is (= '(form-macro 1 2 3) (form-macro 1 2 3)))
      ;; &form is the call form, not the form that encloses the call.
      (is (= '(form-macro) (do (form-macro)))))

    (testing "&env is bound to the local environment of the call"
      ;; Only membership is checked: an implementation may also put its own
      ;; bindings in the environment.
      (is (contains? (let [x 1] (env-macro)) 'x))
      (is (not (contains? (let [x 1] (env-macro)) 'y)))
      ;; A local of the caller of the caller is in scope too.
      (is (contains? (let [x 1] (let [y 2] (env-macro))) 'y)))

    (testing "an autogensym in the expansion is a fresh symbol"
      (is (= :internal (auto-gensym-macro)))
      ;; The macro's `x#` does not capture the caller's `x`.
      (is (= [:internal :caller] (let [x :caller] [(auto-gensym-macro) x])))
      ;; The macro name is qualified because `macroexpand-1` runs at runtime,
      ;; where an unqualified symbol would not reach the macro's var.
      (let [expansion (macroexpand-1 '(clojure.core-test.defmacro/auto-gensym-macro))
            [_ [auto-sym value] body] expansion]
        (is (symbol? auto-sym))
        (is (= :internal value))
        ;; The same symbol is used in the binding and in the body.
        (is (= auto-sym body))
        ;; `x#` itself is not left in the expansion.
        (is (not= 'x# auto-sym))
        (is (not-any? #{'x#} (tree-seq coll? seq expansion)))))))
