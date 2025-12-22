(ns clojure.core-test.descendants
  (:require [clojure.test :refer [are deftest is testing use-fixtures]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists descendants

  ; Some types for testing descendants by type
  (defprotocol TestDescendantsProtocol)
  (defrecord TestDescendantsRecord [] TestDescendantsProtocol)
  (deftype TestDescendantsType [] TestDescendantsProtocol)

  ; A global hierarchy for testing `descendants tag` and `descendants h tag`
  (def global-hierarchy [[TestDescendantsRecord ::record]
                         [::t ::p-1]
                         [::t ::p-2]
                         [::p-1 'ns/p-0]
                         [::p-2 ::root]
                         ['ns/p-0 ::root]])

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

  ; A hierarchy for testing `descendants h tag`
  (def datatypes
    (-> (make-hierarchy)
        (derive TestDescendantsRecord ::datatype)
        (derive TestDescendantsType ::datatype)
        (derive TestDescendantsType ::mutable)))

  ; Another hierarchy for testing `descendants h tag`
  (def diamond
    (-> (make-hierarchy)
        (derive ::a ::root)
        (derive ::b ::a)
        (derive ::c ::a)
        (derive ::d ::b)
        (derive ::d ::c)))

  (deftest test-descendants

    (testing "descendants tag"

      (testing "returns descendants by relationship globally defined with derive"
        (are [expected tag] (= expected (descendants tag))
                            nil ::t
                            #{::t ::p-1} 'ns/p-0
                            #{::t ::p-1 ::p-2 'ns/p-0} ::root
                            #{::t} ::p-2
                            #{#?(:bb 'clojure.core_test.descendants/TestDescendantsRecord :default TestDescendantsRecord)} ::record))

      (testing "cannot get descendants by type inheritance"
        #?@(:cljs
            [(is (thrown? js/Error (descendants TestDescendantsProtocol)))
             (is (thrown? js/Error (descendants js/Object)))]
            :default
            [(is (nil? (descendants TestDescendantsProtocol)))
             (is (thrown? Exception (descendants Object)))]))

      (testing "does not throw on invalid tag"
        (are [tag] (nil? (descendants tag))
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

    (testing "descendants h tag"

      (testing "returns only descendants declared in h, whether the tag is in global hierarchy or not"
        (are [expected h tag] (= expected (descendants h tag))

                              ; tag in h and not in global hierarchy
                              nil diamond ::d
                              #{::d} diamond ::b
                              #{::b ::c ::d} diamond ::a
                              #?(:bb      #{'clojure.core_test.descendants/TestDescendantsRecord 'clojure.core_test.descendants/TestDescendantsType}
                                 :default #{TestDescendantsRecord TestDescendantsType}) datatypes ::datatype
                              #?(:bb      #{'clojure.core_test.descendants/TestDescendantsType}
                                 :default #{TestDescendantsType}) datatypes ::mutable

                              ; tag in both h and global hierarchy, only descendants in h are returned
                              #{::a ::b ::c ::d} diamond ::root

                              ; tag not in h but in global hierarchy
                              nil datatypes ::root
                              nil datatypes ::p-1
                              nil datatypes ::p-2

                              ; tag neither in h nor in global hierarchy
                              nil datatypes ::d
                              nil datatypes ::b
                              nil datatypes ::a))

      (testing "cannot get descendants by type inheritance, whether the tag is in h or not"
        (are [h] #?(:cljs    (thrown? js/Error (descendants h js/Object))
                    :default (thrown? Exception (descendants h Object)))
                 ; tag in h
                 (derive (make-hierarchy) #?(:cljs js/Object :default Object) ::object)
                 ; tag not in h
                 diamond
                 datatypes))

      (testing "does not throw on invalid tag or hierarchy"
        (are [invalid] (nil? (descendants invalid invalid))
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
