(ns clojure.core-test.derive
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists derive
  (deftest test-derive

    (testing "derive tag parent"
      (are [tag parent] (and (nil? (derive tag parent))
                             (isa? tag parent))
                        ::rect ::shape
                        'n/a 'n/b
                        #?(:cljs js/String :default String) ::object))

    (testing "derive h tag parent"
      (are [expected h tag parent] (= expected (derive h tag parent))

                                   ; `derive h tag parent` seems to accept non-namespaced tag/parent on all platforms
                                   ; https://ask.clojure.org/index.php/14759/derive-tag-parent-accepts-namespaced-keyword-symbol-parent
                                   {:ancestors   {:rect #{:shape}}
                                    :descendants {:shape #{:rect}}
                                    :parents     {:rect #{:shape}}} (make-hierarchy) :rect :shape

                                   {:ancestors   {::rect #{::shape}}
                                    :descendants {::shape #{::rect}}
                                    :parents     {::rect #{::shape}}} (make-hierarchy) ::rect ::shape

                                   {:ancestors   {'n/a #{'n/b}}
                                    :descendants {'n/b #{'n/a}}
                                    :parents     {'n/a #{'n/b}}} (make-hierarchy) 'n/a 'n/b

                                   {:ancestors   {#?(:cljs js/String :default String) #{::object}}
                                    :descendants {::object #{#?(:cljs js/String :default String)}}
                                    :parents     {#?(:cljs js/String :default String) #{::object}}} (make-hierarchy) #?(:cljs js/String :default String) ::object

                                   {:ancestors   {::rect #{::shape}, ::square #{::rect ::shape}}
                                    :descendants {::rect #{::square}, ::shape #{::rect ::square}}
                                    :parents     {::rect #{::shape}, ::square #{::rect}}} (derive (make-hierarchy) ::rect ::shape) ::square ::rect

                                   {:ancestors   {::rect #{::shape}}
                                    :descendants {::shape #{::rect}}
                                    :parents     {::rect #{::shape}}} (derive (make-hierarchy) ::rect ::shape) ::rect ::shape

                                   {:ancestors   {::rect #{::shape}}
                                    :descendants {::shape #{::rect}}
                                    :parents     {::rect #{::shape}}} {:parents {} :descendants {} :ancestors {}} ::rect ::shape))

    (testing "cyclic derivation"
      (is (thrown? #?(:cljs js/Error :cljr Exception :default Error) (derive ::a ::a)))
      (let [h (-> (make-hierarchy) (derive ::a ::b) (derive ::b ::c))]
        (is (thrown? #?(:cljs js/Error :default Exception) (derive h ::c ::a)))))

    (testing "bad shapes"

      (testing "nils"
        (are [tag parent] (thrown? #?(:cljs js/Error :bb Error :default Exception) (derive tag parent))
                          nil nil
                          ::tag nil))

      #?(:bb      "bb allows non-namespaced tags"           ; https://github.com/babashka/babashka/issues/1890
         :cljs    "cljs allows non-namespaced tags"         ; https://ask.clojure.org/index.php/14759/derive-tag-parent-accepts-namespaced-keyword-symbol-parent
         :default (testing "non-namespaced tag"
                    (are [tag parent] (thrown? #?(:cljr Exception :default Error) (derive tag parent))
                                      :a ::b
                                      'a 'n/b)))

      #?(:bb      "bb allows non-namespaced parents"        ; https://github.com/babashka/babashka/issues/1890
         :default (testing "non-namespaced parent"
                    (are [tag parent] (thrown? #?(:cljs js/Error :cljr Exception :default Error) (derive tag parent))
                                      :a :b
                                      ::a :b
                                      'a 'b
                                      'n/a 'b
                                      #?(:cljs js/String :default String) :b)))

      (testing "more invalid parents"
        (are [tag parent] (thrown? #?(:cljs js/Error :bb Error :default Exception) (derive tag parent))
                          ::tag #?(:cljs js/String :default String)
                          ::tag 42
                          ::tag "parent"))

      (testing "invalid hierarchy"
        (are [h tag parent] (thrown? #?(:cljs js/Error :default Exception) (derive h tag parent))
                            nil ::a ::b
                            {} ::a ::b
                            {:parents {} :descendants {}} ::a ::b
                            {:parents nil :descendants {} :ancestors {}} ::a ::b
                            ::z ::a ::b
                            true ::a ::b
                            42 ::a ::b
                            3.14 ::a ::b)))))
