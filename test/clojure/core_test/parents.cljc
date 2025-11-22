(ns clojure.core-test.parents
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists
  parents

  ; Some types for testing parents by type
  (def StringT #?(:cljs js/String :default String))
  (def ObjectT #?(:cljs :clojure.core-test.derive/object :default Object))
  (defprotocol TestParentsProtocol)
  (defrecord TestParentsRecord [] TestParentsProtocol)
  (deftype TestParentsType [] TestParentsProtocol)

  ; A global hierarchy for testing `parents tag` and `parents h tag`
  (derive TestParentsRecord ::record)
  (derive ::t ::p-1)
  (derive ::t ::p-2)
  (derive ::p-1 'ns/p-0)
  (derive ::leaf ::t)

  ; A hierarchy for testing `parents h tag`
  (def datatypes
    (-> (make-hierarchy)
        (derive TestParentsRecord ::datatype)
        (derive TestParentsType ::datatype)
        (derive TestParentsType ::mutable)))

  ; Another hierarchy for testing `parents h tag`
  (def diamond
    (-> (make-hierarchy)
        (derive ::b ::a)
        (derive ::c ::a)
        (derive ::d ::b)
        (derive ::d ::c)
        (derive ::leaf ::d)))

  (deftest test-parents

    (testing "parents tag"

      (testing "returns parents by relationship globally defined with derive"
        (are [expected tag] (= expected (parents tag))
                            #{::t} ::leaf
                            #{::p-1 ::p-2} ::t
                            #{'ns/p-0} ::p-1
                            nil ::p-2)
        #?(:bb      "bb doesn't report parents by relationship globally defined with derive for dynamically generated
                     classes (https://github.com/babashka/babashka/issues/1893)"
           :default (is (contains? (parents TestParentsRecord) ::record))))

      (testing "returns parent by type when tag is a class"
        (is (contains? (parents StringT) ObjectT))
        (is (nil? (parents ObjectT))))

      #?(:bb   "bb doesn't report parents by type for dynamically generated classes"
         :cljs "cljs doesn't report parents by type for dynamically generated classes"
         :default
         (testing "returns parent by type when tag is a dynamically generated class"
           (is (contains? (parents TestParentsType) clojure.core_test.parents.TestParentsProtocol))
           (is (contains? (parents TestParentsRecord) clojure.core_test.parents.TestParentsProtocol))
           (is (nil? (parents TestParentsProtocol)))))

      (testing "does not throw on invalid tag"
        (are [tag] (nil? (parents tag))
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

      (testing "returns only parents declared in h, be tag in global hierarchy or not"

        (testing "when tag is not a class"
          (are [expected h tag] (= expected (parents h tag))
                                ; tag in h and not in global hierarchy
                                #{::b ::c} diamond ::d
                                #{::a} diamond ::b
                                nil diamond ::a
                                ; tag in both h and global hierarchy, only parents in h are returned
                                #{::d} diamond ::leaf
                                ; tag not in h but in global hierarchy
                                nil datatypes ::t
                                nil datatypes ::p-1
                                nil datatypes ::p-2
                                ; tag neither in h nor in global hierarchy
                                nil datatypes ::d
                                nil datatypes ::b
                                nil datatypes ::a))

        (testing "when tag is a class"
          #?(:bb "bb doesn't report parents by relationship declared in h for dynamically generated classes
                  (https://github.com/babashka/babashka/issues/1893)"
             :default
             (are [expected h tag] (= expected (->> (parents h tag) (filter keyword?) set))
                                   ; tag in h and not in global hierarchy
                                   #{::datatype ::mutable} datatypes TestParentsType
                                   ; tag in both h and global hierarchy, only parents in h are returned
                                   #{::datatype} datatypes TestParentsRecord))))

      #?(:cljs "cljs doesn't report parents by type when tag is a class, be tag in h or not"
         :default
         (testing "returns parents by type when tag is a class, be tag in h or not"
           (are [h] (is (contains? (parents h StringT) ObjectT))
                    ; tag in h
                    (assoc (make-hierarchy) StringT ::object)
                    ; tag not in h
                    diamond
                    datatypes)))

      #?(:bb   "bb doesn't report parents by type for dynamically generated classes, be tag in h or not"
         :cljs "cljs doesn't report parents by type for dynamically generated classes, be tag in h or not"
         :default
         (testing "returns parents by type when tag is a dynamically generated class, be tag in h or not"
           (are [h tag] (contains? (parents h tag) clojure.core_test.parents.TestParentsProtocol)
                        ; tag in h
                        datatypes TestParentsType
                        datatypes TestParentsRecord
                        ; tag not in h
                        diamond TestParentsType
                        diamond TestParentsRecord)))

      (testing "not throwing on invalid tag or hierarchy"
        (are [invalid] (nil? (parents invalid invalid))
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
