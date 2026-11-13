(ns clojure.core-test.defmethod
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defmethod

  ;; DOCSTRING:
  ;; clojure.core/defmethod
  ;; [multifn dispatch-val & fn-tail]
  ;; Creates and installs a new method of multimethod associated with dispatch-value. 

  ;; `defmethod` installs a method into the state of a multimethod, so
  ;; every multimethod that the tests below exercise is defined at the top
  ;; level of this namespace, outside of the `deftest`. Each multimethod
  ;; has its own name so that the methods installed for one test cannot
  ;; interfere with another.

  (defmulti by-dispatch-value
    "Dispatches on its argument. Used to test installing a method for a
    dispatch value and invoking it."
    identity)

  (defmethod by-dispatch-value nil [_] :nil-method)
  (defmethod by-dispatch-value :keyword [_] :keyword-method)
  (defmethod by-dispatch-value "a string" [_] :string-method)
  (defmethod by-dispatch-value 42 [_] :number-method)
  (defmethod by-dispatch-value 'a-symbol [_] :symbol-method)
  (defmethod by-dispatch-value [:a :b] [_] :vector-method)

  ;; A method installed for the :default dispatch value is used for every
  ;; dispatch value that has no method of its own.
  (defmulti with-default identity)
  (defmethod with-default :known [_] :known-method)
  (defmethod with-default :default [_] :default-method)

  ;; A multimethod that receives arguments in addition to the dispatch
  ;; argument, so that the method function's `fn-tail` can be exercised.
  (defmulti by-first-argument (fn [dispatch & _] dispatch))

  (defmethod by-first-argument :arguments
    ([dispatch] [:one-argument dispatch])
    ([dispatch x] [:two-arguments dispatch x])
    ([dispatch x & more] [:variadic dispatch x (vec more)]))

  (defmethod by-first-argument :destructured
    [dispatch {:keys [a b]} c]
    [dispatch a b c])

  (defmethod by-first-argument :pre-condition
    [dispatch x]
    {:pre [(pos? x)]}
    [dispatch x])

  ;; These multimethods are used to check the state of a multimethod
  ;; before and after `defmethod` is invoked in the test itself.
  (defmulti returned identity)
  (defmulti redefined identity)
  (defmethod redefined :dispatch-value [_] :original-method)
  (defmulti late-arrival identity)

  (deftest test-defmethod
    (testing "defmethod returns the multimethod it installed the method in"
      (is (identical? returned (defmethod returned :dispatch-value [_] :method)))
      (is (= :method (returned :dispatch-value))))

    (testing "the installed method is invoked for its dispatch value"
      (are [expected dispatch-value] (= expected (by-dispatch-value dispatch-value))
        :nil-method nil
        :keyword-method :keyword
        :string-method "a string"
        :number-method 42
        :symbol-method 'a-symbol
        :vector-method [:a :b])
      ;; A dispatch value that has no method installed for it throws.
      (is (p/thrown? (by-dispatch-value :a-keyword-that-has-no-method))))

    (testing "a method installed for :default is used for unmatched dispatch values"
      ;; Any unmatched value uses the :default method, not just keywords.
      (is (= :default-method (with-default :a-keyword-that-has-no-method)))
      (is (= :default-method (with-default 42)))
      (is (= :default-method (with-default nil)))
      (is (= :default-method (with-default "a string")))
      ;; A dispatch value that has its own method does not use :default.
      (is (= :known-method (with-default :known))))

    (testing "the method function is invoked with the arguments of the call"
      (testing "arity"
        (is (= [:one-argument :arguments] (by-first-argument :arguments)))
        (is (= [:two-arguments :arguments 1] (by-first-argument :arguments 1)))
        (is (= [:variadic :arguments 1 [2 3]] (by-first-argument :arguments 1 2 3)))
        (is (= [:variadic :arguments 1 [2 3 4]] (by-first-argument :arguments 1 2 3 4))))

      (testing "destructuring in the method function's parameters"
        (is (= [:destructured 1 2 3] (by-first-argument :destructured {:a 1 :b 2} 3))))

      (testing "pre-conditions in the method function's body are checked"
        (is (= [:pre-condition 5] (by-first-argument :pre-condition 5)))
        (is (p/thrown? (by-first-argument :pre-condition -5)))))

    (testing "a method installed later for the same dispatch value replaces the method"
      (is (= :original-method (redefined :dispatch-value)))
      (is (identical? redefined (defmethod redefined :dispatch-value [_] :replacement-method)))
      (is (= :replacement-method (redefined :dispatch-value))))

    (testing "a method installed after the multimethod has been invoked is found"
      (is (p/thrown? (late-arrival :late-dispatch-value)))
      (is (identical? late-arrival (defmethod late-arrival :late-dispatch-value [_] :late-method)))
      (is (= :late-method (late-arrival :late-dispatch-value))))

    ;; `defmethod` is a macro, so an invocation that does not match
    ;; `[multifn dispatch-val & fn-tail]` is rejected when it is compiled.
    ;; `eval` is the only portable way to compile a form that this test
    ;; cannot contain, and it is not available in ClojureScript outside of
    ;; self-hosted ClojureScript, so this section is skipped there.
    #?(:cljs nil
       :default
       (testing "bad invocations are rejected when compiled"
         (testing "too few arguments"
           (is (p/thrown? (eval '(defmethod))))
           (is (p/thrown? (eval '(defmethod clojure.core-test.defmethod/by-dispatch-value)))))

         (testing "no method function"
           (is (p/thrown? (eval '(defmethod clojure.core-test.defmethod/by-dispatch-value
                                    :a-keyword-that-has-no-method)))))

         (testing "the multimethod does not exist"
           (is (p/thrown? (eval '(defmethod this-multimethod-does-not-exist
                                    :dispatch-value [_] :method)))))))))
