(ns clojure.core-test.vary-meta
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vary-meta
  (deftest test-vary-meta
    (testing "add metadata with assoc"
      (let [v [1 2 3]
            v-with-meta (vary-meta v assoc :tag :vector)]
        (is (= [1 2 3] v-with-meta))
        (is (= :vector (:tag (meta v-with-meta))))
        (is (nil? (:tag (meta v))))))

    (testing "update existing metadata"
      (let [v (with-meta [1 2 3] {:count 3})
            v-updated (vary-meta v assoc :tag :vector)]
        (is (= {:count 3 :tag :vector} (meta v-updated)))))

    (testing "dissoc metadata"
      (let [v (with-meta [1 2 3] {:a 1 :b 2})
            v-dissoc (vary-meta v dissoc :a)]
        (is (= {:b 2} (meta v-dissoc)))))

    (testing "update metadata value"
      (let [v (with-meta [] {:count 0})
            v-inc (vary-meta v update :count inc)]
        (is (= 1 (:count (meta v-inc))))))

    (testing "on symbols"
      (let [s 'foo
            s-meta (vary-meta s assoc :doc "A symbol")]
        (is (= "A symbol" (:doc (meta s-meta))))))

    (testing "on lists"
      (let [l '(1 2 3)
            l-meta (vary-meta l assoc :type :list)]
        (is (= '(1 2 3) l-meta))
        (is (= :list (:type (meta l-meta))))))

    (testing "on maps"
      (let [m {:a 1}
            m-meta (vary-meta m assoc :validated true)]
        (is (= {:a 1} m-meta))
        (is (true? (:validated (meta m-meta))))))

    (testing "on sets"
      (let [s #{1 2 3}
            s-meta (vary-meta s assoc :sorted false)]
        (is (= #{1 2 3} s-meta))
        (is (false? (:sorted (meta s-meta))))))

    (testing "with merge function"
      (let [v (with-meta [1 2] {:a 1 :b 2})
            v-merged (vary-meta v merge {:b 3 :c 4})]
        (is (= {:a 1 :b 3 :c 4} (meta v-merged)))))

    (testing "preserves object value"
      (let [v [1 2 3]
            v-meta (vary-meta v assoc :tag :vector)]
        (is (= v v-meta))
        (is (not (identical? v v-meta)))))

    (testing "multiple operations"
      (let [v []
            v1 (vary-meta v assoc :a 1)
            v2 (vary-meta v1 assoc :b 2)
            v3 (vary-meta v2 assoc :c 3)]
        (is (= {:a 1 :b 2 :c 3} (meta v3)))))

    (testing "with nil metadata"
      (let [v [1 2 3]
            v-meta (vary-meta v assoc :key :value)]
        (is (= :value (:key (meta v-meta))))))

    (testing "replace all metadata"
      (let [v (with-meta [1] {:old true})
            v-new (vary-meta v (constantly {:new true}))]
        (is (= {:new true} (meta v-new)))))

    (testing "on functions"
      (let [f (fn [] 42)
            f-meta (vary-meta f assoc :doc "Returns 42")]
        (is (= 42 (f-meta)))
        (is (= "Returns 42" (:doc (meta f-meta))))))

    (testing "chain multiple vary-meta calls"
      (let [result (-> []
                       (vary-meta assoc :a 1)
                       (vary-meta assoc :b 2)
                       (vary-meta update :a inc))]
        (is (= {:a 2 :b 2} (meta result)))))

    (testing "with complex metadata"
      (let [v []
            v-meta (vary-meta v assoc :info {:author "test" :version 1})]
        (is (= {:author "test" :version 1} (:info (meta v-meta))))))

    (testing "original object unchanged"
      (let [v (with-meta [1 2] {:original true})
            v-new (vary-meta v assoc :modified true)]
        (is (= {:original true} (meta v)))
        (is (= {:original true :modified true} (meta v-new)))))

    (testing "on lazy sequences"
      (let [s (map inc [1 2 3])
            s-meta (vary-meta s assoc :lazy true)]
        (is (= true (:lazy (meta s-meta))))))

    (testing "remove all metadata"
      (let [v (with-meta [1] {:a 1 :b 2})
            v-empty (vary-meta v (constantly nil))]
        (is (nil? (meta v-empty)))))))
