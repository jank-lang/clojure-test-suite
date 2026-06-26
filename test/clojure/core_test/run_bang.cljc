(ns clojure.core-test.run-bang
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.test :refer [are deftest is testing]]))

(when-var-exists run!

  (deftest test-run!
    (testing "Always Nil"
      (are [coll] (nil? (run! identity coll))
        []
        [1]
        [1 2 3]
        ["foo" "bar"]))

    (testing "Causes Side-Effects"
      (let [calls     (volatile! 0)
            inc-calls (fn [_] (vswap! calls inc))]
        (run! inc-calls nil)
        (is (zero? @calls))

        (run! inc-calls [])
        (is (zero? @calls))

        (run! inc-calls [0])
        (is (= 1 @calls))

        (run! inc-calls [0 0])
        (is (= 3 @calls))))

    (testing "Seqable is Required"
      (let [sum  (volatile! 0)
            add! (fn [n] (vswap! sum + n))]
        (is (p/thrown? (run! add! true)))
        (is (zero? @sum))
        (run! add! #{1 2 3})
        (is (= 6 @sum))))

    (testing "Passes Collection Sequentially"
      (let [result (volatile! [])
            coll   [:foo "bar" 'baz]]
        (run! (fn [v] (vswap! result conj v)) coll)
        (is (= coll @result))))

    (testing "Terminates on Exception"
      (let [calls (volatile! 0)
            boom! (fn [_]
                    (vswap! calls inc)
                    (throw (ex-info "Boom!" {})))]
        (is (p/thrown? (run! boom! (range 2))))
        (is (= 1 @calls))))

    (testing "Terminates on Reduced"
      (let [calls (volatile! 0)
            done! (fn [_]
                    (vswap! calls inc)
                    (reduced :done))]
        (is (nil? (run! done! (range 2))))
        (is (= 1 @calls))))))
