(ns clojure.core-test.reify
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/reify

  ;; DOCSTRING:
  ;; clojure.core/reify
  ;; [& opts+specs]
  ;; reify creates an object implementing a protocol or interface.
  ;; reify is a macro with the following structure:
  ;; 
  ;; (reify options* specs*)
  ;; 
  ;; Currently there are no options.
  ;; 
  ;; Each spec consists of the protocol or interface name followed by zero
  ;; or more method bodies:
  ;; 
  ;; protocol-or-interface-or-Object
  ;; (methodName [args+] body)*
  ;; 
  ;; Methods should be supplied for all methods of the desired
  ;; protocol(s) and interface(s). You can also define overrides for
  ;; methods of Object. Note that the first parameter must be supplied to
  ;; correspond to the target object ('this' in Java parlance). Thus
  ;; methods for interfaces will take one more argument than do the
  ;; interface declarations.  Note also that recur calls to the method
  ;; head should *not* pass the target object, it will be supplied
  ;; automatically and can not be substituted.
  ;; 
  ;; The return type can be indicated by a type hint on the method name,
  ;; and arg types can be indicated by a type hint on arg names. If you
  ;; leave out all hints, reify will try to match on same name/arity
  ;; method in the protocol(s)/interface(s) - this is preferred. If you
  ;; supply any hints at all, no inference is done, so all hints (or
  ;; default of Object) must be correct, for both arguments and return
  ;; type. If a method is overloaded in a protocol/interface, multiple
  ;; independent method definitions must be supplied.  If overloaded with
  ;; same arity in an interface you must specify complete hints to
  ;; disambiguate - a missing hint implies Object.
  ;; 
  ;; Method heads are recursion points for recur, as in a fn. The method
  ;; bodies of reify are lexical closures, and can refer to the surrounding
  ;; local scope:
  ;; 
  ;; (str (let [f "foo"] 
  ;; (reify Object
  ;; (toString [this] f))))
  ;; == "foo"
  ;; 
  ;; (seq (let [f "foo"] 
  ;; (reify clojure.lang.Seqable
  ;; (seq [this] (seq f)))))
  ;; == (\f \o \o)
  ;; 
  ;; reify always implements clojure.lang.IObj and transfers meta
  ;; data of the form to the created object.
  ;; 
  ;; (meta ^{:k :v} (reify Object (toString [this] "foo")))
  ;; == {:k :v}

  ;; The protocols implemented by the tests below. `reify` is a macro, so
  ;; the specs it references are resolved when the form containing the
  ;; `reify` is compiled, and the protocols must already exist by then.

  (defprotocol TestGreeter
    (greet [this] "Returns a greeting.")
    (greet-named [this name] "Returns a greeting for name."))

  ;; A protocol with no methods, so that its spec has no method bodies.
  (defprotocol TestMarker)

  (defprotocol TestOverloaded
    (overloaded [this] [this a] [this a b]))

  (defprotocol TestCountdown
    (countdown [this n]))

  (deftest test-reify
    (testing "a reify implements the protocols named in its specs"
      (let [greeter (reify TestGreeter
                      (greet [this] "Hello, world")
                      (greet-named [this name] (str "Hello, " name)))]
        (is (= "Hello, world" (greet greeter)))
        (is (= "Hello, Ada" (greet-named greeter "Ada")))
        (is (satisfies? TestGreeter greeter))
        ;; protocols that are not named in the specs are not implemented
        (is (not (satisfies? TestOverloaded greeter))))
      ;; `reify` with no specs at all creates an object that implements
      ;; nothing
      (is (not (satisfies? TestGreeter (reify)))))

    (testing "the first parameter of a method corresponds to the target object"
      (let [o (reify TestGreeter
                (greet [this] this)
                (greet-named [this name] [this name]))]
        (is (identical? o (greet o)))
        (is (identical? o (first (greet-named o "name")))))
      ;; the parameter can have any name, or be `_` where it is not used
      (let [o (reify TestGreeter
                (greet [_] :greeted)
                (greet-named [_ name] name))]
        (is (= :greeted (greet o)))
        (is (= "name" (greet-named o "name")))))

    (testing "one reify can implement several protocols"
      ;; TestMarker has no methods, so its spec has no method bodies, and
      ;; TestOverloaded's method has one body per arity.
      (let [o (reify TestGreeter
                (greet [this] :greeted)
                (greet-named [this name] name)
                TestMarker
                TestOverloaded
                (overloaded [this] :no-args)
                (overloaded [this a] [:one-arg a])
                (overloaded [this a b] [:two-args a b]))]
        (is (= :greeted (greet o)))
        (is (= "name" (greet-named o "name")))
        ;; a protocol with no methods is only observable via satisfies?
        (is (satisfies? TestMarker o))
        (are [expected args] (= expected (apply overloaded o args))
          :no-args        []
          [:one-arg 1]    [1]
          [:two-args 1 2] [1 2])))

    (testing "an Object method can be overridden"
      ;; str calls toString, and the method body closes over a local
      (is (= "foo"
             (str (let [f "foo"]
                    (reify Object (#?(:cljr ToString :default toString) [this] f))))))
      ;; Object can be given along with protocols
      (is (= ["reified" :greeted]
             (let [o (reify Object
                       (#?(:cljr ToString :default toString) [this] "reified")
                       TestGreeter
                       (greet [this] :greeted)
                       (greet-named [this name] name))]
               [(str o) (greet o)]))))

    #?(:bb
       (testing "a method head is a recursion point for recur"
         ;; bb requires the target object as the first argument to a
         ;; recur in a reify method, rather than supplying it
         ;; automatically
         (let [o (reify TestCountdown
                   (countdown [this n]
                     (if (zero? n) :done (recur this (dec n)))))]
           (is (= :done (countdown o 3)))
           ;; recurs in a method do not grow the stack
           (is (= :done (countdown o 10000)))))
       :default
       (testing "a method head is a recursion point for recur"
         ;; recur passes only the method's parameters, not the target object
         (let [o (reify TestCountdown
                   (countdown [this n] (if (zero? n) :done (recur (dec n)))))]
           (is (= :done (countdown o 3)))
           ;; recurs in a method do not grow the stack
           (is (= :done (countdown o 10000))))))

    (testing "method bodies are lexical closures over the surrounding scope"
      ;; reify can implement an interface, here one that closes over a local
      #?(:cljs
         (is (= '(\f \o \o)
                (seq (let [f "foo"]
                       (reify ISeqable (-seq [this] (seq f)))))))
         :default
         (is (= '(\f \o \o)
                (seq (let [f "foo"]
                       (reify clojure.lang.Seqable (seq [this] (seq f))))))))
      ;; locals, including mutable state, are shared by the method bodies
      (let [calls (atom 0)
            o (reify TestGreeter
                (greet [this] (swap! calls inc))
                (greet-named [this name] [@calls name]))]
        (is (= 1 (greet o)))
        (is (= [1 "name"] (greet-named o "name"))))
      ;; a reify can be created in the body of another reify's method
      (is (= :inner
             (greet (reify TestGreeter
                      (greet [this] (greet (reify TestGreeter
                                             (greet [_] :inner)
                                             (greet-named [_ name] name))))
                      (greet-named [this name] name))))))

    (testing "the metadata of the form is transferred to the created object"
      ;; reify always implements clojure.lang.IObj, so the created object
      ;; carries the metadata of the form. Only the :k entry is checked
      ;; because some implementations add their own (such as :line).
      (is (= :v (:k (meta ^{:k :v} (reify TestMarker)))))
      ;; implementing clojure.lang.IObj means with-meta works, and the
      ;; object keeps implementing its protocols
      (let [o (with-meta (reify TestGreeter
                           (greet [this] :greeted)
                           (greet-named [this name] name))
                {:a 1})]
        (is (= {:a 1} (meta o)))
        (is (= :greeted (greet o)))))

    (testing "the return type can be hinted on the method name or the argument
              vector, and an argument type on an argument name"
      (is (= :greeted (greet (reify TestGreeter
                               (^Object greet [this] :greeted)
                               (greet-named [this name] name)))))
      (let [o (reify TestGreeter
                (greet ^Object [this] :greeted)
                (greet-named [this ^Object name] name))]
        (is (= :greeted (greet o)))
        (is (= "name" (greet-named o "name")))))))
