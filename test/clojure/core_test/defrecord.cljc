(ns clojure.core-test.defrecord
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/defrecord

  ;; DOCSTRING:
  ;; clojure.core/defrecord
  ;; [name [& fields] & opts+specs]
  ;; (defrecord name [fields*]  options* specs*)
  ;; 
  ;; Options are expressed as sequential keywords and arguments (in any order).
  ;; 
  ;; Supported options:
  ;; :load-ns - if true, importing the record class will cause the
  ;; namespace in which the record was defined to be loaded.
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
  ;; The class will have the (immutable) fields named by
  ;; fields, which can have type hints. Protocols/interfaces and methods
  ;; are optional. The only methods that can be supplied are those
  ;; declared in the protocols/interfaces.  Note that method bodies are
  ;; not closures, the local environment includes only the named fields,
  ;; and those fields can be accessed directly.
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
  ;; The class will have implementations of several (clojure.lang)
  ;; interfaces generated automatically: IObj (metadata support) and
  ;; IPersistentMap, and all of their superinterfaces.
  ;; 
  ;; In addition, defrecord will define type-and-value-based =,
  ;; and will defined Java .hashCode and .equals consistent with the
  ;; contract for java.util.Map.
  ;; 
  ;; When AOT compiling, generates compiled bytecode for a class with the
  ;; given name (a symbol), prepends the current ns as the package, and
  ;; writes the .class file to the *compile-path* directory.
  ;; 
  ;; Two constructors will be defined, one taking the designated fields
  ;; followed by a metadata map (nil for none) and an extension field
  ;; map (nil for none), and one taking only the fields (using nil for
  ;; meta and extension fields). Note that the field names __meta,
  ;; __extmap, __hash and __hasheq are currently reserved and should not
  ;; be used when defining your own records.
  ;; 
  ;; Given (defrecord TypeName ...), two factory functions will be
  ;; defined: ->TypeName, taking positional parameters for the fields,
  ;; and map->TypeName, taking a map of keywords to field values.

  ;; `defrecord` defines a new type at compile time, so every record that
  ;; these tests exercise is defined here, at the top level of this
  ;; namespace, rather than inside the `deftest` below.

  ;; The simplest kind of record: a name and some fields.
  (defrecord Point [x y])

  ;; A record with the same fields and values as `Point`, but a different
  ;; type. It shows that record equality is type-based.
  (defrecord Point2D [x y])

  (when-var-exists defprotocol
    ;; Records can implement protocols, whose methods receive the record as
    ;; their first argument.
    (defprotocol Shaped
      (describe [this])
      (scaled [this k]))

    ;; A method body accesses the fields directly by name, can create new
    ;; instances of the record through its factory functions, and refers to
    ;; the record class by its unqualified name.
    (defrecord Packet [label width height]
      Shaped
      (describe [this] (when (instance? Packet this)
                         {:label label :width width :height height}))
      (scaled [_this k] (->Packet label (* width k) (* height k)))))

  (deftest test-defrecord
    (testing "the positional factory takes the fields in order"
      (are [expected field] (= expected (get (->Point 1 2) field))
        1 :x
        2 :y
        nil :z))

    (testing "the map factory maps keywords to field values"
      (is (= (->Point 1 2) (map->Point {:x 1 :y 2})))
      ;; Keys that are not fields end up in the extension map.
      (is (= 3 (:z (map->Point {:x 1 :y 2 :z 3})))))

    (testing "two constructors are defined: the fields, or the fields plus metadata and extension map"
      (is (= (->Point 1 2) (Point. 1 2)))
      (let [p (Point. 1 2 {:source ::ctor} {:extra 3})]
        (is (= 1 (:x p)))
        (is (= 2 (:y p)))
        (is (= 3 (:extra p)))
        (is (= {:source ::ctor} (meta p)))))

    (testing "= and hash are type-and-value-based"
      (is (= (->Point 1 2) (->Point 1 2)))
      (is (not= (->Point 1 2) (->Point 2 1)))
      ;; Same fields and values, but a different type.
      (is (not= (->Point 1 2) (->Point2D 1 2)))
      ;; A record is not a plain map, even though it behaves like one.
      (is (not= (->Point 1 2) {:x 1 :y 2}))
      (is (not= {:x 1 :y 2} (->Point 1 2)))
      ;; Extension-map entries are part of the value.
      (is (not= (->Point 1 2) (assoc (->Point 1 2) :z 3)))
      (is (= (assoc (->Point 2 1) :z 3) (map->Point {:x 2 :y 1 :z 3})))
      (is (= (hash (->Point 1 2)) (hash (Point. 1 2)))))

    (testing "a record behaves like a map of its fields"
      (let [p (->Point 1 2)]
        ;; Probes of the generated map implementation, not a test of the
        ;; map fns themselves. Entry order is not specified, so compare
        ;; whole maps rather than `keys`/`seq` order.
        (are [expected actual] (= expected actual)
          true (map? p)
          2 (count p)
          {:x 1 :y 2} (into {} p)
          [:x 1] (find p :x)
          true (contains? p :x)
          false (contains? p :z))))

    (testing "assoc of a field name yields a record with that field updated"
      (is (= (->Point 10 2) (assoc (->Point 1 2) :x 10))))

    (testing "a name that is not a field goes to the extension map"
      (let [p (assoc (->Point 1 2) :z 3)]
        (is (= 3 (:z p)))
        ;; Entry order is not specified, so compare the key set.
        (is (= #{:x :y :z} (set (keys p))))
        (is (= (->Point 1 2) (dissoc p :z)))))

    (testing "dissoc of a field name yields a plain map"
      (is (= {:y 2} (dissoc (->Point 1 2) :x))))

    (testing "supports metadata"
      (let [p (with-meta (->Point 1 2) {:source ::meta})]
        (is (= {:source ::meta} (meta p)))
        ;; Metadata does not participate in equality.
        (is (= (->Point 1 2) p))))

    (when-var-exists defprotocol
      (testing "implements the protocol methods given after its field list"
        ;; Calling the protocol fns is what proves that the method bodies
        ;; supplied in the defrecord form became the record's implementation.
        (let [p (->Packet :small 1 2)]
          (is (= {:label :small :width 1 :height 2} (describe p)))
          (is (= (->Packet :small 2 4) (scaled p 2))))))

    (testing "the factories take one argument per field"
      ;; ClojureScript's factories fill missing fields with nil and ignore
      ;; extra arguments instead of checking the number of arguments.
      #?(:cljs (are [expected rec] (= expected rec)
                  (Point. 1 nil) (->Point 1)
                  (Point. 1 2) (->Point 1 2 3)
                  (Point. nil nil) (map->Point))
         :default (are [form] (p/thrown? form)
                     (->Point 1)
                     (->Point 1 2 3)
                     (map->Point))))))
