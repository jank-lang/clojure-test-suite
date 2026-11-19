(ns clojure.core-test.defonce
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defonce

  ;; DOCSTRING:
  ;; clojure.core/defonce
  ;; [name expr]
  ;; defs name to have the root value of the expr iff the named var has no root value,
  ;; else expr is unevaluated

  ;; `defonce` defines a var when it is compiled, so every var exercised below
  ;; is defined at the top level of this namespace, outside of the `deftest`.
  ;; The `defonce` forms that share a name take effect in load order, so
  ;; reading the var afterwards shows which of them won.

  ;; The named var has no root value yet, so its expr supplies the root value.
  (defonce fresh :root-value)

  ;; The named var already has a root value, so the second expr is ignored.
  (defonce already-bound :first-value)
  (defonce already-bound :second-value)

  ;; The expr of an ignored `defonce` is unevaluated, so the counter is only
  ;; stepped by the first one.
  (def counter (atom 0))
  (defonce counted (swap! counter inc))
  (defonce counted (swap! counter inc))

  ;; "No root value" and "a root value of nil (or false)" are different states,
  ;; and the docstring's condition is only about the first one. nil and false
  ;; are ordinary root values, so these vars are left alone and their exprs are
  ;; not evaluated.
  ;;
  ;; The catch is that `nil` is the absence-of-evidence value: asserting that
  ;; `nil-rooted` is nil proves nothing on its own, because it would be nil
  ;; whether the expr ran or not. So the expr here is a `swap!` on a counter
  ;; that starts at 0, and the counter staying at 0 is what actually shows the
  ;; expr was unevaluated. The var's own value is asserted separately, only to
  ;; show that defonce left it nil rather than replacing it.
  (def nil-rooted nil)
  (def nil-rooted-evaluations (atom 0))
  (defonce nil-rooted (swap! nil-rooted-evaluations inc))
  (def false-rooted false)
  (defonce false-rooted :not-used)

  ;; A `declare`d var has no root value, so `defonce` supplies one, and the
  ;; second `defonce` of it is ignored.
  (declare declared-not-bound)
  (defonce declared-not-bound :declared-value)
  (defonce declared-not-bound :ignored-value)

  ;; So does a `def` with no initializer: the var exists but has no root
  ;; value, so the `defonce` that follows it supplies one.
  (def uninitialized)
  (defonce uninitialized :defonce-value)
  (defonce uninitialized :ignored-value)

  ;; The same thing again, with the expr evaluating something, to show that it
  ;; runs for the first `defonce` and does not run for the second.
  (def uninitialized-evaluations (atom 0))
  (def counted-only-once)
  (defonce counted-only-once (swap! uninitialized-evaluations inc))
  (defonce counted-only-once (swap! uninitialized-evaluations inc))

  ;; The expr is evaluated where the `defonce` form appears, and the value it
  ;; produces becomes the root value of the var.
  (def evaluated (atom :not-evaluated))
  (defonce from-expr
    (do
      (reset! evaluated :evaluated)
      [:computed :value]))

  ;; `defonce` is allowed anywhere `def` is, so this function defines the var
  ;; on its first call and leaves it alone afterwards.
  (defn defonce-in-body [v]
    (defonce body-var v)
    body-var)

  (deftest test-defonce
    (testing "defs name to have the root value of the expr"
      ;; The var is defined, and referring to it reads the root value.
      (is (= :root-value fresh))
      ;; The expr is evaluated where the `defonce` appears, and its value is
      ;; the root value.
      (is (= :evaluated @evaluated))
      (is (= [:computed :value] from-expr)))

    (testing "iff the named var has no root value"
      ;; A second `defonce` of a name leaves the root value alone.
      (is (= :first-value already-bound))
      ;; A nil or false root value is still a root value, so these vars were
      ;; left alone. Note that `nil` here is the value defonce kept, not proof
      ;; that it did any keeping: `nil-rooted-evaluations` staying at 0, in the
      ;; "else expr is unevaluated" section below, is the real evidence.
      (is (nil? nil-rooted))
      (is (false? false-rooted))
      ;; A `declare`d var has no root value, so the second `defonce` won.
      (is (= :declared-value declared-not-bound)))

    (testing "a var defined without an initializer has no root value"
      ;; `(def uninitialized)` creates the var but leaves it unbound, so the
      ;; `defonce` gave it the value of its expr.
      (is (= :defonce-value uninitialized))
      ;; The first `defonce` ran its expr, the second did not.
      (is (= 1 counted-only-once))
      (is (= 1 @uninitialized-evaluations)))

    (testing "else expr is unevaluated"
      (is (= 1 counted))
      (is (= 1 @counter))
      ;; The same evidence for the nil-rooted var: its expr would have stepped
      ;; this counter if it had run. A nil var alone would not prove this.
      (is (= 0 @nil-rooted-evaluations)))

    (testing "a defonce in a function body keeps the first value"
      (is (= :first-call (defonce-in-body :first-call)))
      (is (= :first-call (defonce-in-body :second-call))))))
