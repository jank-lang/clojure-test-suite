(ns clojure.core-test.assert
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/assert

  ;; DOCSTRING:
  ;; clojure.core/assert
  ;; [x]
  ;; [x message]
  ;; Evaluates expression x and throws an AssertionError with optional
  ;; message if x does not evaluate to logical true.
  ;; 
  ;; Assertion checks are omitted from compiled code if '*assert*' is
  ;; false.

  (deftest test-assert
    (testing "returns nil and throws nothing when x is logical true"
      (are [x] (nil? (assert x))
        true
        1
        0
        -1
        0.0
        ""
        "a string"
        :a-keyword
        'a-symbol
        []
        [1 2 3]
        '()                 ; the empty list is logical true
        '(1 2 3)
        {}
        {:a 1}
        #{}
        #{1 2 3}
        (range)
        (identity true))

      ;; the value of x is discarded; the result of the check is nil
      (is (nil? (assert :not-nil))))

    (testing "throws when x is logical false"
      (is (p/thrown? (assert nil)))
      (is (p/thrown? (assert false)))
      (is (p/thrown? (assert nil "some message")))
      (is (p/thrown? (assert false "some message")))
      (is (p/thrown? (assert (= 1 2))))
      (is (p/thrown? (assert (= 1 2) "one is not two"))))

    (testing "x is evaluated exactly once"
      (let [calls (volatile! 0)
            x     (fn [v] (vswap! calls inc) v)]
        (is (nil? (assert (x true))))
        (is (= 1 @calls))
        (is (nil? (assert (x 1) "the message form is not evaluated on success")))
        (is (= 2 @calls))
        (is (p/thrown? (assert (x false))))
        (is (= 3 @calls))))

    (testing "message may be any value when the check fails"
      (are [message] (p/thrown? (assert false message))
        "a string"
        :a-keyword
        'a-symbol
        42
        nil
        ["a" "compound" "message"]
        (str "a " "computed " "message"))))

  ;; The comments above describe what a check does when it is compiled
  ;; in. The other half of the docstring, that checks are omitted from
  ;; compiled code when *assert* is false, is tested below.
  ;;
  ;; *assert* is consulted by the 'assert' macro while the form is
  ;; being expanded, so it is a compile-time switch, not a run-time
  ;; one: a program turns it off while its code is compiled and the
  ;; checks are then simply absent from the built code. Two separate
  ;; deftests make that reproducible, because each deftest is compiled
  ;; exactly once, as the file is read. Whatever *assert* says at that
  ;; moment decides what goes into the compiled test function; by the
  ;; time the tests are run, *assert* has long been true again.
  ;;
  ;; In ClojureScript checks are elided by the
  ;; :elide-asserts compiler option, which applies to the whole build,
  ;; and a set! in a loaded file runs only after that file has already
  ;; been expanded. Babashka is special in a different way: it refuses
  ;; set! on the root binding, but honours the thread bindings.
  #?(:bb      (push-thread-bindings {#'*assert* false})
     :cljs    nil
     :default (set! *assert* false))

  #?(:cljs    nil     ; without the set! above, these checks would be compiled in
     :default
     (deftest test-assert-elided
       (let [evaluations (volatile! 0)
             x           (fn [v] (vswap! evaluations inc) v)
             message     (fn [] (vswap! evaluations inc) "a message")]
         (testing "a check compiled while *assert* was false never fires"
           (is (nil? (assert (x false))))
           (is (nil? (assert (x nil))))
           (is (nil? (assert (= 1 2)))))

         (testing "and nothing it was given is evaluated"
           (is (zero? @evaluations))
           (is (nil? (assert (x false))))
           (is (nil? (assert (x nil) (message))))
           (is (nil? (assert (= 1 2) (message))))
           (is (zero? @evaluations))))))

  #?(:bb      (pop-thread-bindings)
     :cljs    nil
     :default (set! *assert* true)))
