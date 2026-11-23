(ns clojure.core-test.defonce
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/defonce

  ;; CLOJURESCRIPT BUILD MODES: ClojureScript has no runtime primitive for
  ;; `defonce`. It is a macro that expands to
  ;;
  ;;   (when-not (exists? x) (def x init))
  ;;
  ;; and `exists?` is a macro too, whose whole job is to emit a JavaScript
  ;; `typeof` check. So "does `x` already have a value?" is answered by code in
  ;; the generated JavaScript, not by inspecting a var. A dev build emits this
  ;; for `(defonce state :initial)`:
  ;;
  ;;   if ((typeof demo !== 'undefined') &&
  ;;       (typeof demo.core !== 'undefined') &&
  ;;       (typeof demo.core.state !== 'undefined')) {
  ;;   } else {
  ;;     demo.core.state = new cljs.core.Keyword(null,"initial",...);
  ;;   }
  ;;
  ;; That is the guard doing its job: the assignment is conditional.
  ;;
  ;; A shadow-cljs *release* build emits the namespace as an object literal and
  ;; flattens the statements into a single scope. The namespace object plainly
  ;; exists by then, so the `typeof` check is folded away and only the
  ;; assignment is left:
  ;;
  ;;   var demo = {core: {}};
  ;;   demo.core.state = cljs$cst$294$initial;
  ;;
  ;; `defonce` has become `def`. This happens in release builds at every
  ;; optimization level (`:simple` and `:none` alike), and inside function
  ;; bodies as well as at namespace scope. Dev builds keep the check.
  ;;
  ;; "Does not overwrite an existing root value" therefore remains correct
  ;; ClojureScript, but it is not *observable* in a release build: the guard is
  ;; not in the output. Those assertions are wrapped in `guard-preserved?`,
  ;; which is true for dev builds and for every other dialect, so they all run
  ;; and all pass under a ClojureScript dev build. This is a toolchain
  ;; artifact, not a ClojureScript semantic.
  ;;
  ;; Separately, ClojureScript has no unbound var: `(declare x)` and `(def x)`
  ;; emit no JavaScript at all, so `defonce` always runs and assertions about
  ;; those vars would hold for a reason unrelated to their premise. They are
  ;; excluded for ClojureScript in every build mode with a reader conditional.


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

  ;; Whether `defonce`'s runtime guard is present in this build, which decides
  ;; whether the assertions about leaving an existing root value alone can run
  ;; at all; see the note at the top of the file.
  (defn guard-preserved?
    "Is `defonce`'s runtime guard present in this build?

    ClojureScript compiles `defonce` into a `typeof` probe, via `exists?`. A
    shadow-cljs *release* build types each namespace as a non-null object
    literal in one flat scope, so Closure folds that probe to `false` and
    deletes it. That happens at every optimization level (`:simple` and `:none`
    alike) and inside function bodies as well as at namespace scope. Assertions
    that `defonce` left an existing root value alone are unobservable there and
    skip themselves.

    `goog.DEBUG` tracks the build mode: true for a shadow-cljs dev build, false
    for a release build. It is a proxy for whether the guard survived, not the
    same fact, so revisit this if shadow-cljs ever decouples the two. Every
    other dialect keeps the guard at all times, so this is always true there."
    []
    #?(:cljs (true? goog.DEBUG)
       :default true))

  (deftest test-defonce
    (testing "defs name to have the root value of the expr"
      ;; The var is defined, and referring to it reads the root value.
      (is (= :root-value fresh))
      ;; The expr is evaluated where the `defonce` appears, and its value is
      ;; the root value.
      (is (= :evaluated @evaluated))
      (is (= [:computed :value] from-expr)))

    (testing "iff the named var has no root value"
      ;; These need `defonce`'s runtime guard; see the note at the top of the
      ;; file.
      (when (guard-preserved?)
        ;; A second `defonce` of a name leaves the root value alone.
        (is (= :first-value already-bound))
        ;; A nil or false root value is still a root value, so these vars were
        ;; left alone. Note that `nil` here is the value defonce kept, not
        ;; proof that it did any keeping: `nil-rooted-evaluations` staying at
        ;; 0, in the "else expr is unevaluated" section below, is the real
        ;; evidence.
        (is (nil? nil-rooted))
        (is (false? false-rooted)))
      ;; A `declare`d var has no root value, so the second `defonce` won.
      ;; ClojureScript has no unbound var, though: `(declare x)` emits no
      ;; JavaScript, so this premise does not hold there in any build mode.
      #?(:cljs nil
         :default (is (= :declared-value declared-not-bound))))

    (testing "a var defined without an initializer has no root value"
      ;; `(def uninitialized)` creates the var but leaves it unbound, so the
      ;; `defonce` gave it the value of its expr. ClojureScript has no unbound
      ;; var for this to be about: `(def uninitialized)` emits no JavaScript.
      #?(:cljs nil
         :default (is (= :defonce-value uninitialized)))
      ;; The first `defonce` ran its expr, the second did not. This needs the
      ;; runtime guard; see the note at the top of the file.
      (when (guard-preserved?)
        (is (= 1 counted-only-once))
        (is (= 1 @uninitialized-evaluations))))

    (testing "else expr is unevaluated"
      ;; These need the runtime guard; see the note at the top of the file.
      (when (guard-preserved?)
        (is (= 1 counted))
        (is (= 1 @counter))
        ;; The same evidence for the nil-rooted var: its expr would have stepped
        ;; this counter if it had run. A nil var alone would not prove this.
        (is (= 0 @nil-rooted-evaluations))))

    (testing "a defonce in a function body keeps the first value"
      ;; The first call takes the var from unbound to bound, which every build
      ;; preserves. The second call needs the runtime guard, which a release
      ;; build strips even inside function bodies.
      (is (= :first-call (defonce-in-body :first-call)))
      (when (guard-preserved?)
        (is (= :first-call (defonce-in-body :second-call)))))))
