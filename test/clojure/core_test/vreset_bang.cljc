(ns clojure.core-test.vreset-bang
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists vreset!
  (deftest test-vreset-bang
    (testing "reset to new value"
      (let [v (volatile! 42)]
        (vreset! v 100)
        (is (= 100 @v))))

    (testing "returns new value"
      (let [v (volatile! 1)]
        (is (= 99 (vreset! v 99)))))

    (testing "reset to nil"
      (let [v (volatile! 42)]
        (vreset! v nil)
        (is (nil? @v))))

    (testing "reset to same value"
      (let [v (volatile! 42)]
        (vreset! v 42)
        (is (= 42 @v))))

    (testing "reset to different types"
      (let [v (volatile! 42)]
        (vreset! v "string")
        (is (= "string" @v))
        (vreset! v [1 2 3])
        (is (= [1 2 3] @v))
        (vreset! v {:a 1})
        (is (= {:a 1} @v))))

    (testing "multiple resets"
      (let [v (volatile! 0)]
        (vreset! v 1)
        (is (= 1 @v))
        (vreset! v 2)
        (is (= 2 @v))
        (vreset! v 3)
        (is (= 3 @v))))

    (testing "reset with boolean"
      (let [v (volatile! true)]
        (vreset! v false)
        (is (false? @v))
        (vreset! v true)
        (is (true? @v))))

    (testing "reset with collection"
      (let [v (volatile! [])]
        (vreset! v [1 2 3])
        (is (= [1 2 3] @v))
        (vreset! v #{:a :b})
        (is (= #{:a :b} @v))))

    (testing "reset with keyword"
      (let [v (volatile! :old)]
        (vreset! v :new)
        (is (= :new @v))))

    (testing "reset with symbol"
      (let [v (volatile! 'old)]
        (vreset! v 'new)
        (is (= 'new @v))))

    (testing "reset with function"
      (let [v (volatile! +)]
        (vreset! v *)
        (is (= 6 (@v 2 3)))))

    (testing "reset from nil"
      (let [v (volatile! nil)]
        (vreset! v 42)
        (is (= 42 @v))))

    (testing "reset in sequence"
      (let [v (volatile! 0)]
        (doseq [i (range 10)]
          (vreset! v i))
        (is (= 9 @v))))

    (testing "reset with zero"
      (let [v (volatile! 100)]
        (vreset! v 0)
        (is (= 0 @v))))

    (testing "reset with negative"
      (let [v (volatile! 42)]
        (vreset! v -42)
        (is (= -42 @v))))

    (testing "reset with string"
      (let [v (volatile! "old")]
        (vreset! v "new")
        (is (= "new" @v))
        (vreset! v "")
        (is (= "" @v))))

    (testing "reset with empty collections"
      (let [v (volatile! [1 2 3])]
        (vreset! v [])
        (is (= [] @v)))
      (let [v (volatile! {:a 1})]
        (vreset! v {})
        (is (= {} @v))))

    (testing "reset with large number"
      (let [v (volatile! 0)]
        (vreset! v 1000000000000)
        (is (= 1000000000000 @v))))

    (testing "reset with computed value"
      (let [v (volatile! 0)]
        (vreset! v (+ 1 2 3))
        (is (= 6 @v))))

    (testing "reset with character"
      #?(:clj
         (let [v (volatile! \a)]
           (vreset! v \z)
           (is (= \z @v)))))

    (when-var-exists volatile?
      (testing "preserves volatile identity"
        (let [v (volatile! 1)]
          (vreset! v 2)
          (is (volatile? v)))))

    (testing "multiple volatiles reset independently"
      (let [v1 (volatile! 1)
            v2 (volatile! 2)]
        (vreset! v1 10)
        (vreset! v2 20)
        (is (= 10 @v1))
        (is (= 20 @v2))))

    (testing "reset with nested structure"
      (let [v (volatile! {})]
        (vreset! v {:a {:b {:c 1}}})
        (is (= {:a {:b {:c 1}}} @v))))))
