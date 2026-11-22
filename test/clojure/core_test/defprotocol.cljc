(ns clojure.core-test.defprotocol
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/defprotocol

  ;; DOCSTRING:
  ;; clojure.core/defprotocol
  ;; [name & opts+sigs]
  ;; A protocol is a named set of named methods and their signatures:
  ;; (defprotocol AProtocolName
  ;; 
  ;; ;optional doc string
  ;; "A doc string for AProtocol abstraction"
  ;; 
  ;; ;options
  ;; :extend-via-metadata true
  ;; 
  ;; ;method signatures
  ;; (bar [this a b] "bar docs")
  ;; (baz [this a] [this a b] [this a b c] "baz docs"))
  ;; 
  ;; No implementations are provided. Docs can be specified for the
  ;; protocol overall and for each method. The above yields a set of
  ;; polymorphic functions and a protocol object. All are
  ;; namespace-qualified by the ns enclosing the definition The resulting
  ;; functions dispatch on the type of their first argument, which is
  ;; required and corresponds to the implicit target object ('this' in 
  ;; Java parlance). defprotocol is dynamic, has no special compile-time 
  ;; effect, and defines no new types or classes. Implementations of 
  ;; the protocol methods can be provided using extend.
  ;; 
  ;; When :extend-via-metadata is true, values can extend protocols by
  ;; adding metadata where keys are fully-qualified protocol function
  ;; symbols and values are function implementations. Protocol
  ;; implementations are checked first for direct definitions (defrecord,
  ;; deftype, reify), then metadata definitions, then external
  ;; extensions (extend, extend-type, extend-protocol)
  ;; 
  ;; defprotocol will automatically generate a corresponding interface,
  ;; with the same name as the protocol, i.e. given a protocol:
  ;; my.ns/Protocol, an interface: my.ns.Protocol. The interface will
  ;; have methods corresponding to the protocol functions, and the
  ;; protocol will automatically work with instances of the interface.
  ;; 
  ;; Note that you should not use this interface with deftype or
  ;; reify, as they support the protocol directly:
  ;; 
  ;; (defprotocol P 
  ;; (foo [this]) 
  ;; (bar-me [this] [this y]))
  ;; 
  ;; (deftype Foo [a b c] 
  ;; P 
  ;; (foo [this] a)
  ;; (bar-me [this] b)
  ;; (bar-me [this y] (+ c y)))
  ;; 
  ;; (bar-me (Foo. 1 2 3) 42)
  ;; => 45
  ;; 
  ;; (foo 
  ;; (let [x 42]
  ;; (reify P 
  ;; (foo [this] 17)
  ;; (bar-me [this] x)
  ;; (bar-me [this y] x))))
  ;; => 17

  ;; `defprotocol` defines its vars when it is compiled, and `defrecord`,
  ;; `deftype`, `reify` and `extend-type` implement a protocol when they are
  ;; compiled, so all of them are at the top level of this namespace, outside
  ;; of the `deftest`.

  (defprotocol Named
    "A thing that has a name."
    (name-of [this] "Returns the name of the thing.")
    (greet [this] [this greeting] "Greets the thing."))

  ;; A protocol whose doc string, and whose method's doc string, are omitted.
  (defprotocol Undocumented
    (undocumented [this]))

  ;; A type that implements the protocol in its own definition.
  (deftype Person [n]
    Named
    (name-of [_] [:person n])
    (greet [_] [:person :hello n])
    (greet [_ greeting] [:person greeting n]))

  ;; A record that implements the protocol in its own definition.
  (defrecord Robot [id]
    Named
    (name-of [_] [:robot id])
    (greet [_] [:robot :hello id])
    (greet [_ greeting] [:robot greeting id]))

  ;; A type that implements nothing.
  (deftype Unextended [])

  ;; A protocol with the option, the protocol doc string and the method doc
  ;; strings, in the arrangement the docstring gives.
  (defprotocol Describable
    "A thing that can describe itself."
    :extend-via-metadata true
    (describe [this] "Describes the thing.")
    (describe-with [this detail] "Describes the thing, with a detail."))

  ;; The same protocol, without the option.
  (defprotocol Undescribable
    "A thing that cannot be extended with metadata."
    (undescribe [this] "Describes the thing."))

  ;; A record that implements nothing, but records support metadata.
  (defrecord Bare [])

  ;; A record that implements the protocol in its own definition.
  (defrecord SelfDescribing []
    Describable
    (describe [_] :direct)
    (describe-with [_ detail] [:direct detail]))

  ;; A record that is extended after the fact.
  (defrecord ExternallyDescribing [])
  (extend-type ExternallyDescribing
    Describable
    (describe [_] :external)
    (describe-with [_ detail] [:external detail]))

  (deftest test-defprotocol
    (testing "the protocol functions dispatch on the type of their first argument"
      ;; Each implementation tags its result with a value local to itself
      ;; (`:person` or `:robot`), and both are given the same name, so the only
      ;; way the results can differ is that a different implementation ran.
      (are [expected form] (= expected form)
        [:person :ada] (name-of (->Person :ada))
        [:robot :ada] (name-of (->Robot :ada))
        [:person :hello :ada] (greet (->Person :ada))
        [:robot :hello :ada] (greet (->Robot :ada))
        [:person :good-morning :ada] (greet (->Person :ada) :good-morning)
        [:robot :good-morning :ada] (greet (->Robot :ada) :good-morning))
      ;; An implementation supplied by `reify`.
      (let [reified (reify Named
                      (name-of [_] :reified)
                      (greet [_] [:hello :reified])
                      (greet [_ greeting] [greeting :reified]))]
        (is (= :reified (name-of reified)))
        (is (= [:hello :reified] (greet reified)))
        (is (= [:good-morning :reified] (greet reified :good-morning)))))

    (testing "a call that matches none of the declared arities throws"
      ;; `apply` is used because a direct call with the wrong number of
      ;; arguments to a protocol function is rejected when it is compiled.
      ;; Too few arguments, and more arguments than any declared arity.
      (is (p/thrown? (apply name-of [])))
      (is (p/thrown? (apply greet [(->Person :ada) :good-morning :punctuation]))))

    (testing "no implementations are provided by the protocol itself"
      ;; The functions dispatch on the type of the first argument, so a value
      ;; whose type implements nothing fails. `nil` is such a value.
      (are [form] (p/thrown? form)
        (name-of (Unextended.))
        (greet (Unextended.) :good-morning)
        (name-of nil)
        (greet nil :good-morning)))

    (testing "the protocol and its functions are namespace-qualified by the enclosing namespace"
      (are [v] (= "clojure.core-test.defprotocol" (name (ns-name (:ns (meta v)))))
        #'Named
        #'name-of
        #'greet))

    (testing "docs can be specified for the protocol overall and for each method"
      ;; How a doc string reaches a var's metadata is already covered by the
      ;; `defn` and `meta` tests; here only the strings matter.
      (are [expected-doc v] (= expected-doc (:doc (meta v)))
        "A thing that has a name."       #'Named
        "Returns the name of the thing." #'name-of
        "Greets the thing."              #'greet
        ;; The docs are optional.
        nil #'Undocumented
        nil #'undocumented))

    (testing "with :extend-via-metadata true, metadata supplies the implementation"
      ;; The key is the fully-qualified protocol function symbol, and the value
      ;; is the function that implements it. Note that ` (syntax-quote)
      ;; expands a symbol to its fully-qualified form, while ' (quote) does not.
      (is (= :from-metadata
             (describe (with-meta (->Bare) {`describe (fn [_] :from-metadata)}))))
      ;; The implementing function is passed the arguments of the call.
      (is (= [:from-metadata :detail]
             (describe-with (with-meta (->Bare) {`describe-with
                                                 (fn [_ detail] [:from-metadata detail])})
                            :detail)))
      ;; An unqualified key is not the fully-qualified symbol, so it is ignored.
      (is (p/thrown? (describe (with-meta (->Bare) {'describe (fn [_] :from-metadata)})))))

    (testing "without the option, metadata supplies no implementation"
      (is (p/thrown? (undescribe (with-meta (->Bare) {`undescribe (fn [_] :from-metadata)})))))

    ;; Order: direct definition -> metadata -> external extension
    (testing "metadata is checked before external extensions"
      (is (= :external (describe (->ExternallyDescribing))))
      (is (= [:external :detail] (describe-with (->ExternallyDescribing) :detail)))
      (is (= :from-metadata
             (describe (with-meta (->ExternallyDescribing)
                         {`describe (fn [_] :from-metadata)}))))
      (is (= [:from-metadata :detail]
             (describe-with (with-meta (->ExternallyDescribing)
                              {`describe-with (fn [_ detail] [:from-metadata detail])})
                            :detail))))

    (testing "a direct definition is checked before a metadata definition"
      (let [self (->SelfDescribing)]
        (is (= :direct (describe self)))
        (is (= [:direct :detail] (describe-with self :detail)))
        ;; ClojureScript checks a value's metadata before its own definition,
        ;; contrary to the order the docstring gives.
        (is (= #?(:cljs :from-metadata
                  :default :direct)
               (describe (with-meta self {`describe (fn [_] :from-metadata)}))))))))
