(ns clojure.core-test.underive
  (:require [clojure.test :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists
  underive

  (def shape-hierarchy
    (-> (make-hierarchy)
        (derive ::circle ::shape)
        (derive ::rect ::shape)
        (derive ::square ::rect)))

  (def diamond-hierarchy
    (-> (make-hierarchy)
        (derive ::b ::a)
        (derive ::c ::a)
        (derive ::d ::b)
        (derive ::d ::c)))

  (deftest test-underive

    (testing "underive tag parent"

      (testing "when tag is child of parent"
        (derive ::t ::p)
        (is (isa? ::t ::p))
        (is (nil? (underive ::t ::p)))
        (is (not (isa? ::t ::p))))

      (testing "when tag is not child of parent"
        (are [tag parent] (nil? (underive tag parent))
                          ::square ::rect
                          ::square ::shape
                          ::rect ::shape
                          nil nil
                          :a :a
                          'a 'b
                          42 3.14
                          true false)))

    (testing "underive h tag parent"

      (testing "when tag is child of parent in h"
        (are [expected h tag parent] (= expected (underive h tag parent))

                                     {:ancestors   {::circle #{::shape}, ::rect #{::shape}}
                                      :descendants {::shape #{::circle ::rect}}
                                      :parents     {::circle #{::shape}, ::rect #{::shape}}} shape-hierarchy ::square ::rect

                                     {:ancestors   {::circle #{::shape}, ::square #{::rect}}
                                      :descendants {::shape #{::circle}, ::rect #{::square}}
                                      :parents     {::circle #{::shape}, ::square #{::rect}}} shape-hierarchy ::rect ::shape

                                     {:parents     {::b #{::a}, ::c #{::a}, ::d #{::c}},
                                      :ancestors   {::b #{::a}, ::c #{::a}, ::d #{::a ::c}},
                                      :descendants {::a #{::d ::b ::c}, ::c #{::d}}} diamond-hierarchy ::d ::b

                                     {:parents     {::b #{::a}, ::c #{::a}},
                                      :ancestors   {::b #{::a}, ::c #{::a}},
                                      :descendants {::a #{::b ::c}}} (underive diamond-hierarchy ::d ::b) ::d ::c))

      (testing "when tag is not child of parent in h"
        (are [h tag parent] (= h (underive h tag parent))
                            shape-hierarchy ::rect ::square
                            shape-hierarchy ::square ::shape
                            diamond-hierarchy ::b ::d
                            diamond-hierarchy ::d ::a)))

    (testing "bad shape"
      (are [h tag parent] (thrown? #?(:cljs js/Error :default Exception) (underive h tag parent))
                          nil ::a ::b
                          {} ::a ::b
                          [[:parents {}] [:descendants {}] [:ancestors {}]] ::a ::b
                          ::z ::a ::b
                          true ::a ::b
                          42 ::a ::b
                          3.14 ::a ::b))))
