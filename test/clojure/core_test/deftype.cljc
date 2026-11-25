(ns clojure.core-test.deftype
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/deftype

  ;; DOCSTRING:
  ;; clojure.core/deftype
  ;; [name [& fields] & opts+specs]
  ;; (deftype name [fields*]  options* specs*)
  ;; 
  ;; Options are expressed as sequential keywords and arguments (in any order).
  ;; 
  ;; Supported options:
  ;; :load-ns - if true, importing the type class will cause the
  ;; namespace in which the type was defined to be loaded.
  ;; Defaults to false.
  ;; 
  ;; Each spec consists of a protocol or interface name followed by zero
  ;; or more method bodies:
  ;; 
  ;; protocol-or-interface-or-Object
  ;; (methodName [args*] body)*
  ;; 
  ;; Dynamically generates compiled bytecode for class with the given
  ;; name, in a package with the same name as the current namespace, the
  ;; given fields, and, optionally, methods for protocols and/or
  ;; interfaces. 
  ;; 
  ;; The class will have the (by default, immutable) fields named by
  ;; fields, which can have type hints. Protocols/interfaces and methods
  ;; are optional. The only methods that can be supplied are those
  ;; declared in the protocols/interfaces.  Note that method bodies are
  ;; not closures, the local environment includes only the named fields,
  ;; and those fields can be accessed directly. Fields can be qualified
  ;; with the metadata :volatile-mutable true or :unsynchronized-mutable
  ;; true, at which point (set! afield aval) will be supported in method
  ;; bodies. Note well that mutable fields are extremely difficult to use
  ;; correctly, and are present only to facilitate the building of higher
  ;; level constructs, such as Clojure's reference types, in Clojure
  ;; itself. They are for experts only - if the semantics and
  ;; implications of :volatile-mutable or :unsynchronized-mutable are not
  ;; immediately apparent to you, you should not be using them.
  ;; 
  ;; Method definitions take the form:
  ;; 
  ;; (methodname [args*] body)
  ;; 
  ;; The argument and return types can be hinted on the arg and
  ;; methodname symbols. If not supplied, they will be inferred, so type
  ;; hints should be reserved for disambiguation.
  ;; 
  ;; Methods should be supplied for all methods of the desired
  ;; protocol(s) and interface(s). You can also define overrides for
  ;; methods of Object. Note that a parameter must be supplied to
  ;; correspond to the target object ('this' in Java parlance). Thus
  ;; methods for interfaces will take one more argument than do the
  ;; interface declarations. Note also that recur calls to the method
  ;; head should *not* pass the target object, it will be supplied
  ;; automatically and can not be substituted.
  ;; 
  ;; In the method bodies, the (unqualified) name can be used to name the
  ;; class (for calls to new, instance? etc).
  ;; 
  ;; When AOT compiling, generates compiled bytecode for a class with the
  ;; given name (a symbol), prepends the current ns as the package, and
  ;; writes the .class file to the *compile-path* directory.
  ;; 
  ;; One constructor will be defined, taking the designated fields.  Note
  ;; that the field names __meta, __extmap, __hash and __hasheq are currently
  ;; reserved and should not be used when defining your own types.
  ;; 
  ;; Given (deftype TypeName ...), a factory function called ->TypeName
  ;; will be defined, taking positional parameters for the fields

  ;; `deftype` defines a class when it is compiled, so the protocols and the
  ;; types that the tests below exercise are defined at the top level of this
  ;; namespace, outside of the `deftest`. Overrides of `Object`'s methods are
  ;; not exercised because the method names differ between dialects (they are
  ;; covered by the `reify` tests).

  ;; --- Protocols implemented by the types below. ---

  ;; One method with more than one arity.
  (defprotocol Shape
    (area [this] [this factor]))

  (defprotocol Describe
    (describe [this]))

  ;; A protocol that declares no methods.
  (defprotocol Marker)

  ;; The methods through which the mutable fields of `Slot` are observed.
  (defprotocol Cell
    (cell-value [this])
    (cell-tag [this])
    (set-cell! [this new-value new-tag]))

  (defprotocol NodeOps
    (this-node [this])
    (node? [this x])
    (make-node [this value]))

  (defprotocol Countdown
    (countdown [this n]))

  (defprotocol LoadKind
    (load-kind [this]))

  ;; --- Types. ---

  ;; Fields, but no specs.
  (deftype Point [x y])

  ;; Fields with type hints.
  (deftype Hinted [^long n ^String s])

  ;; Mutable fields, one of each kind.
  (deftype Slot [^:volatile-mutable value ^:unsynchronized-mutable tag]
    Cell
    (cell-value [_] value)
    (cell-tag [_] tag)
    (set-cell! [_ new-value new-tag]
      (set! value new-value)
      (set! tag new-tag)
      [value tag]))

  ;; The fields are in scope in the method bodies, one type can implement more
  ;; than one protocol, and all arities of a protocol's method are supplied.
  (deftype Square [side]
    Shape
    (area [_] (* side side))
    (area [_ factor] (* factor side side))
    Describe
    (describe [_] [:square side]))

  ;; No fields, and a protocol that declares no methods.
  (deftype Tagged [] Marker)

  ;; The unqualified class name can be used in the method bodies, and the first
  ;; parameter of every method is the target object.
  (deftype Node [value]
    NodeOps
    (this-node [this] this)
    (node? [_ x] (instance? Node x))
    (make-node [_ v] (new Node v)))

  ;; Per the docstring, a `recur` to a method head does not pass the target
  ;; object.
  (deftype Down []
    Countdown
    (countdown [_ n] (if (zero? n) :done (recur (dec n)))))

  ;; Babashka and ClojureScript reject the `:load-ns` option.
  #?@(:bb []
      :cljs []
      :default
      [(deftype LoadNs []
         :load-ns true
         LoadKind
         (load-kind [_] :loaded))])

  (deftest test-deftype
    (testing "the class has the fields named by `fields`, and `->TypeName` takes them positionally"
      ;; `->TypeName` takes positional parameters for the fields, and `new`
      ;; invokes the one constructor of the generated class.
      (are [expected point] (= expected [(.-x point) (.-y point)])
        [1 2]       (->Point 1 2)
        [3 4]       (new Point 3 4)
        [nil false] (->Point nil false)))

    (testing "the generated class is a class of its own"
      (is (instance? Point (->Point 1 2)))
      (is (not (instance? Square (->Point 2 2)))))

    (testing "protocols and methods are optional"
      ;; `Point` has fields but no specs, so no protocol is satisfied.
      (is (not (satisfies? Shape (->Point 1 2)))))

    (testing "the fields can have type hints"
      (let [hinted (->Hinted 7 "seven")]
        (is (= 7 (.-n hinted)))
        (is (= "seven" (.-s hinted)))))

    (testing "the fields are in scope in the method bodies"
      (let [square (->Square 3)]
        (is (= 9 (area square)))
        ;; All arities of a protocol method are supplied.
        (is (= 18 (area square 2)))))

    (testing "a type can implement more than one protocol"
      (let [square (->Square 3)]
        (is (satisfies? Shape square))
        (is (satisfies? Describe square))
        (is (= [:square 3] (describe square)))))

    (testing "a protocol can declare no methods"
      (is (satisfies? Marker (->Tagged))))

    (testing "fields marked :volatile-mutable or :unsynchronized-mutable can be set! in method bodies"
      (let [slot (->Slot 1 :original)]
        (is (= 1 (cell-value slot)))
        (is (= :original (cell-tag slot)))
        (is (= [2 :updated] (set-cell! slot 2 :updated)))
        (is (= [2 :updated] [(cell-value slot) (cell-tag slot)]))))

    (testing "the unqualified class name can be used in the method bodies"
      (let [node (->Node 1)]
        ;; The first parameter of every method is the target object.
        (is (identical? node (this-node node)))
        ;; `instance?` on the unqualified class name.
        (is (node? node node))
        ;; `new` on the unqualified class name.
        (is (= 2 (.-value (make-node node 2))))))

    (testing "a recur to a method head does not pass the target object"
      (is (= :done (countdown (->Down) 3))))

    ;; See the comment above the definition of `LoadNs`.
    #?(:bb nil
       :cljs nil
       :default
       (testing "a type can be given the :load-ns option"
         (is (= :loaded (load-kind (->LoadNs))))))

    ;; `deftype` is a macro, so a definition that does not match the contract is
    ;; rejected when it is compiled. `eval` is the only portable way to compile
    ;; a form that this test cannot contain, and it is not available in
    ;; ClojureScript outside of self-hosted ClojureScript, so this section is
    ;; skipped there.
    #?(:cljs nil
       :default
       (testing "a definition that does not match the contract is rejected when compiled"
         ;; "the local environment includes only the named fields"
         #?@(:bb [] ; Babashka is built on sci, which lets method bodies capture locals.
             :default
             [(testing "a method body cannot refer to a local"
                (is (p/thrown? (eval '(let [local 1]
                                       (deftype clojure.core-test.deftype/NoClosure []
                                         clojure.core-test.deftype/NodeOps
                                         (this-node [_] local)
                                         (node? [_ x] x)
                                         (make-node [_ v] v)))))))])

         ;; Fields are immutable unless they are marked mutable.
         (testing "(set! afield aval) requires a mutable field"
           (is (p/thrown? (eval '(deftype clojure.core-test.deftype/Immutable [x]
                                   clojure.core-test.deftype/NodeOps
                                   (this-node [_] (set! x 1))
                                   (node? [_ y] y)
                                   (make-node [_ v] v))))))

         ;; "The only methods that can be supplied are those declared in the
         ;; protocols/interfaces."
         (testing "a method that no protocol declares is rejected"
           (is (p/thrown? (eval '(deftype clojure.core-test.deftype/Undeclared []
                                   clojure.core-test.deftype/NodeOps
                                   (this-node [_] :this-node)
                                   (node? [_ y] y)
                                   (make-node [_ v] v)
                                   (no-such-method [_] 1))))))))))
