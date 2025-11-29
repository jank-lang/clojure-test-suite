(ns clojure.core-test.ancestors
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists ancestors

  ; Some classes for testing ancestors by type inheritance
  (def AncestorT #?(:cljs js/Object :default Object))
  (def ChildT #?(:cljs :default :default clojure.lang.PersistentHashSet))

  ; Some custom types for testing ancestors by type inheritance
  (defprotocol TestAncestorsProtocol)
  (defrecord TestAncestorsRecord [] TestAncestorsProtocol)
  (deftype TestAncestorsType [] TestAncestorsProtocol)

  ; A global hierarchy for testing `ancestors tag` and `ancestors h tag`
  (def global-hierarchy [[TestAncestorsRecord ::record]
                         [::record ::object]
                         [::leaf ::t]
                         [::t ::p-1]
                         [::t ::p-2]
                         [::p-1 'ns/p-0]])

  (defn register-global-hierarchy []
    (doseq [[tag parent] global-hierarchy]
      (derive tag parent)))

  (defn unregister-global-hierarchy []
    (doseq [[tag parent] global-hierarchy]
      (underive tag parent)))

  (defn with-global-hierarchy [tests]
    (register-global-hierarchy)
    (tests)
    (unregister-global-hierarchy))

  (use-fixtures :once with-global-hierarchy)

  ; A hierarchy for testing `ancestors h tag`
  (def datatypes
    (-> (make-hierarchy)
        (derive TestAncestorsRecord ::datatype)
        (derive TestAncestorsType ::datatype)
        (derive TestAncestorsType ::mutable)
        (derive ::datatype ::type)))

  ; Another hierarchy for testing `ancestors h tag`
  (def diamond
    (-> (make-hierarchy)
        (derive ::b ::a)
        (derive ::c ::a)
        (derive ::d ::b)
        (derive ::d ::c)
        (derive ::leaf ::d)))

  (deftest test-ancestors

    (testing "ancestors tag"

      (testing "returns ancestors by relationship globally defined with derive"
        (are [expected tag] (= expected (ancestors tag))
                            #{::t ::p-1 ::p-2 'ns/p-0} ::leaf
                            #{::p-1 ::p-2 'ns/p-0} ::t
                            #{'ns/p-0} ::p-1
                            nil ::p-2)
        #?(:bb      "bb doesn't report ancestors by relationship globally defined with derive for custom types
                   (https://github.com/babashka/babashka/issues/1893)"
           :default (is (= #{::record ::object} (->> (ancestors TestAncestorsRecord)
                                                     (filter keyword?) ; filter out parents by type, tested in next sections
                                                     set)))))

      (testing "returns ancestors by type inheritance when tag is a class"
        #?(:cljs "cljs doesn't report ancestors by type inheritance yet (CLJS-3464)"
           :clj  (is (contains? (ancestors ChildT) AncestorT))))

      #?(:bb      "bb doesn't report ancestors by type inheritance for custom types"
         :cljs    "cljs doesn't report ancestors by type inheritance yet (CLJS-3464)"
         :default (testing "returns ancestors by type inheritance when tag is a custom type"
                    (is (contains? (ancestors TestAncestorsType) clojure.core_test.ancestors.TestAncestorsProtocol))
                    (is (contains? (ancestors TestAncestorsRecord) clojure.core_test.ancestors.TestAncestorsProtocol))
                    (is (contains? (ancestors TestAncestorsRecord) clojure.lang.Associative))
                    (is (nil? (ancestors TestAncestorsProtocol)))))

      (testing "does not throw on invalid tag"
        (are [tag] (nil? (ancestors tag))
                   nil
                   "anything"
                   42
                   3.14
                   true
                   false
                   []
                   {}
                   #{}
                   '())))

    (testing "parents h tag"

      (testing "returns only ancestors declared in h, whether the tag is in global hierarchy or not"
        (are [expected h tag] (= expected (->> (ancestors h tag)
                                               (filter keyword?) ; filter out ancestors by type, tested in next sections
                                               set))

                              ; tag in h and not in global hierarchy
                              #{::a ::b ::c} diamond ::d
                              #{::a} diamond ::b
                              #{} diamond ::a
                              #?@(; bb doesn't report ancestors by relationship declared in h for custom types
                                  ; (https://github.com/babashka/babashka/issues/1893)
                                  :bb      []
                                  :default [#{::datatype ::mutable ::type} datatypes TestAncestorsType])

                              ; tag in both h and global hierarchy, only ancestors in h are returned
                              #{::a ::b ::c ::d} diamond ::leaf
                              #?@(; bb doesn't report ancestors by relationship declared in h for custom types
                                  ; (https://github.com/babashka/babashka/issues/1893)
                                  :bb      []
                                  :default [#{::datatype ::type} datatypes TestAncestorsRecord])

                              ; tag not in h but in global hierarchy
                              #{} datatypes ::t
                              #{} datatypes ::p-1
                              #{} datatypes ::p-2

                              ; tag neither in h nor in global hierarchy
                              #{} datatypes ::d
                              #{} datatypes ::b
                              #{} datatypes ::a))

      #?(:cljs    "cljs doesn't report ancestors by type inheritance yet (CLJS-3464)"
         :default (testing "returns ancestors by type inheritance when tag is a class, whether the tag is in h or not"
                    (are [h] (contains? (ancestors h ChildT) AncestorT)
                             ; tag in h
                             (derive (make-hierarchy) ChildT ::object)
                             ; tag not in h
                             diamond
                             datatypes)))

      #?(:bb      "bb doesn't report ancestors by type inheritance for custom types"
         :cljs    "cljs doesn't report ancestors by type inheritance yet (CLJS-3464)"
         :default (testing "returns ancestors by type inheritance when tag is a custom type, whether the tag is in h or not"
                    (are [h tag] (let [actual-ancestors (ancestors h tag)]
                                   (and (contains? actual-ancestors clojure.core_test.ancestors.TestAncestorsProtocol)
                                        (contains? actual-ancestors clojure.lang.Associative)))
                                 ; tag in h
                                 datatypes TestAncestorsRecord
                                 ; tag not in h
                                 diamond TestAncestorsRecord)))

      (testing "does not throw on invalid tag or hierarchy"
        (are [invalid] (nil? (ancestors invalid invalid))
                       nil
                       "anything"
                       42
                       3.14
                       true
                       false
                       []
                       {}
                       #{}
                       '())))))
