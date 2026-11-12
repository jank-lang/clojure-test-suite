(ns clojure.core-test.defn-
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defn-

  ;; DOCSTRING:
  ;; clojure.core/defn-
  ;; [name & decls]
  ;; same as defn, yielding non-public def

  ;; `defn-` is `defn` with `:private true` added to the metadata of the
  ;; name, so everything else it accepts -- arities, doc-strings,
  ;; attr-maps, prepost maps and bodies -- behaves exactly as it does for
  ;; `defn` and is covered by the `defn` tests. These tests rely on `defn`
  ;; for all of that and pin down only what `defn-` adds: the resulting
  ;; var is not public.
  ;;
  ;; `defn-` expands to a `def` at compile time, so the functions below
  ;; are defined at the top level of this namespace, outside of the
  ;; `deftest`.

  ;; A plain `defn`, for contrast with the `defn-` definitions.
  (defn public-fn [x] (inc x))

  (defn- private-fn [x] (inc x))
  (defn- private-prepost-fn [x] {:pre [(pos? x)]} (* 2 x))

  ;; A private function can still call itself by name.
  (defn- private-recursive-fn
    [n]
    (if (zero? n)
      0
      (+ n (private-recursive-fn (dec n)))))

  (defn- private-documented-fn
    "Returns its argument."
    {:custom-attr :custom-value}
    [x]
    x)

  (defn- private-multi-arity-fn
    ([] :no-arguments)
    ([x] [:one-argument x])
    ([x y & more] [:variadic x y more]))

  (deftest test-defn-
    (testing "yields a non-public def"
      (are [v] (true? (:private (meta v)))
        #'private-fn
        #'private-prepost-fn
        #'private-recursive-fn
        #'private-documented-fn
        #'private-multi-arity-fn)

      (is (not (:private (meta #'public-fn)))
          "a plain `defn` does not yield a private def"))

    (testing "the var is interned in its namespace, but not among its public vars"
      (when-var-exists clojure.core/ns-interns
        (are [sym] (contains? (ns-interns 'clojure.core-test.defn-) sym)
          'private-fn
          'private-documented-fn
          'private-multi-arity-fn))

      (when-var-exists clojure.core/ns-publics
        (is (contains? (ns-publics 'clojure.core-test.defn-) 'public-fn))
        (are [sym] (not (contains? (ns-publics 'clojure.core-test.defn-) sym))
          'private-fn
          'private-documented-fn
          'private-multi-arity-fn)))

    (testing "a private var is still an ordinary function within its own namespace"
      (is (= 2 (private-fn 1)))
      (is (= 6 (private-recursive-fn 3)))
      (is (= 4 (private-prepost-fn 2)))
      (is (p/thrown? (private-prepost-fn 0)))   ; prepost maps still apply
      (is (= [:variadic 1 2 [3 4]] (private-multi-arity-fn 1 2 3 4)))

      ;; It works when passed around as a value and when reached through
      ;; its var, just like the function behind any other var.
      (is (= [2 3 4] (map private-fn [1 2 3])))
      (is (= 2 (apply private-fn [1])))
      (is (= 2 (@#'private-fn 1))))

    (testing "privacy is added to, rather than a substitute for, the rest of the var's metadata"
      (is (= "Returns its argument." (:doc (meta #'private-documented-fn))))
      (is (= :custom-value (:custom-attr (meta #'private-documented-fn))))
      (is (= 'private-fn (:name (meta #'private-fn))))
      (are [v arglists] (= arglists (:arglists (meta v)))
        #'private-fn             '([x])
        #'private-multi-arity-fn '([] [x] [x y & more])))

    ;; Another namespace can neither refer to the var nor call it by its
    ;; qualified name. Clojure JVM and ClojureCLR enforce that when the
    ;; form is compiled, which is what the `eval` calls below observe.
    ;; ClojureScript enforces it in the compiler, where a test cannot
    ;; reach, and Babashka's sci does not enforce it at all, so both are
    ;; skipped.
    #?(:cljs nil
       :bb   nil
       :default
       (testing "another namespace cannot refer to or call a private var"
         (is (p/thrown?
               (binding [*ns* (create-ns 'clojure.core-test.defn-.consumer)]
                 (eval '(clojure.core-test.defn-/private-fn 1)))))

         (is (p/thrown?
               (binding [*ns* (create-ns 'clojure.core-test.defn-.consumer)]
                 (eval '(ns clojure.core-test.defn-.consumer
                          (:require [clojure.core-test.defn- :refer [private-fn]]))))))

         ;; A public var from the same namespace is, of course, reachable.
         (is (= 2 (binding [*ns* (create-ns 'clojure.core-test.defn-.consumer)]
                    (eval '(clojure.core-test.defn-/public-fn 1)))))))))
