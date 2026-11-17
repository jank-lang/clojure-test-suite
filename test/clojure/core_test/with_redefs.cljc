(ns clojure.core-test.with-redefs
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/with-redefs

  ;; DOCSTRING:
  ;; clojure.core/with-redefs
  ;; [bindings & body]
  ;; binding => var-symbol temp-value-expr
  ;; 
  ;; Temporarily redefines Vars while executing the body.  The
  ;; temp-value-exprs will be evaluated and each resulting value will
  ;; replace in parallel the root value of its Var.  After the body is
  ;; executed, the root values of all the Vars will be set back to their
  ;; old values.  These temporary changes will be visible in all threads.
  ;; Useful for mocking out functions during testing.

  ;; `with-redefs` redefines existing vars, so the vars used below have to
  ;; be defined at the top level of this namespace, outside of the
  ;; `deftest`.
  (def value :original)
  (def other-value :other-original)
  (defn add-one [n] (inc n))
  (defn read-value [] value)
  (def meta-value (with-meta [1 2 3] {:kind :original}))

  (deftest test-with-redefs
    (testing "redefining a var"
      (is (= :original value) "Baseline value, before any redefinition")
      (is (= :redefined (with-redefs [value :redefined] value))
          "The body sees the temporary value")
      (is (= :redefined (with-redefs [value :redefined] (read-value)))
          "The temporary value is visible through a function that reads the var")
      (is (= :original value)
          "The old root value is restored once the body is done")
      (is (= 20 (with-redefs [add-one (fn [n] (* 10 n))] (add-one 2)))
          "A function can be redefined")
      (is (= 3 (add-one 2)) "The original function is restored")
      (is (nil? (with-redefs [value nil] value))
          "A var can be redefined to nil")
      (is (= :original value) "And restored afterwards"))

    (testing "values of the body"
      (is (= :body-value (with-redefs [value :redefined] :body-value))
          "The value of the body is returned")
      (is (= :second (with-redefs [value :redefined] :first :second))
          "The last form of the body provides the return value")
      (is (nil? (with-redefs [value :redefined]))
          "An empty body returns nil"))

    (testing "empty bindings vector"
      (is (= :body-value (with-redefs [] :body-value))
          "The body is executed with no redefinitions")
      (is (= :original (with-redefs [] (read-value)))))

    (testing "temporary values are evaluated before any var is redefined"
      (is (= [:redefined :original]
             (with-redefs [value :redefined
                           other-value value]
               [value other-value]))
          "`other-value` receives the pre-redefinition value of `value`")
      (is (= :other-original other-value) "Both vars are restored"))

    (testing "a temporary value can be lazy"
      (is (= '(0 1 2 3)
             (with-redefs [value (range)] (take 4 value)))
          "An infinite temporary value is not realized by the redefinition"))

    (testing "nested redefinitions"
      (is (= [:inner :outer]
             (with-redefs [value :outer]
               [(with-redefs [value :inner] value)
                value]))
          "The inner redefinition is undone when its body is done")
      (is (= :original value) "The outer redefinition is undone as well"))

    (testing "vars are restored even when the body throws"
      (is (p/thrown? (with-redefs [value :redefined]
                       (throw (ex-info "boom" {}))))
          "The exception propagates out of `with-redefs`")
      (is (= :original value) "The old root value is restored"))

    (testing "a restored value keeps its metadata"
      (is (= {:kind :original} (meta meta-value))
          "Baseline metadata, before any redefinition")
      (is (nil? (meta (with-redefs [meta-value [:redefined]] meta-value)))
          "The temporary value brings its own metadata")
      (is (= {:kind :original} (meta meta-value))
          "The metadata of the original value is still there once restored")
      (is (= [1 2 3] meta-value) "And so is the value itself"))

    ;; CLJS has no threads, so there is no other thread to observe the change.
    #?@(:cljs []
        :default
        [(testing "redefinitions are visible in all threads"
           (let [started (promise)
                 seen (promise)]
             (future
               @started
               (deliver seen (read-value)))
             (with-redefs [value :redefined]
               (deliver started :go)
               (is (= :redefined (deref seen 5000 ::timeout))
                   "Another thread sees the redefinition"))
             (is (= :original value) "And the old root value is restored")))])))
