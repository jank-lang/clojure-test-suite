(ns clojure.core-test.run-bang
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.test :refer [are deftest is testing]]))

(when-var-exists run!

  (defn inspect-run! [proc init coll]
    (let [result (volatile! init)]
      (run! (fn [v] (vswap! result proc v)) coll)
      @result))

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
        (run! inc-calls [])
        (is (zero? @calls))

        (run! inc-calls [0])
        (is (= 1 @calls))

        (run! inc-calls [0 0])
        (is (= 3 @calls))))

    (testing "Collection Argument"
      (are [coll] (= 6 (inspect-run! + 0 coll))
        [1 2 3]
        '(1 2 3)
        #{1 2 3})

      (testing "Nil"
        (is (zero? (inspect-run! + 0 nil))))

      (testing "Strings"
        (are [expected s] (= expected (inspect-run! conj [] s))
          [] ""
          [\a] "a"
          [\f \o \o] "foo"))

      ;; phel passes values to the proc (other dialects pass [k v] entries)
      (testing "Maps"
        (are [expected m] (= expected (inspect-run! conj #{} m))
          #{} {}
          #{[:foo :bar]} {:foo :bar}
          #{[:foo :bar] [:baz :buzz]} {:foo :bar :baz :buzz}))

      (testing "Non-Seqable Values"
        (are [v] (p/thrown? (run! identity v))
          ;; basilisp, phel, and cljs interpret chars as strings
          #?@(:lpy [] :phel [] :cljs [] :default [\a])
          true
          #uuid "00000000-0000-0000-0000-000000000000"
          1
          1.0
          :foo
          'foo)))

    (testing "Passes Collection Sequentially"
      (let [coll [:foo "bar" 'baz]]
        (is (= coll (inspect-run! conj [] coll)))))

    (testing "Terminates on Exception"
      (let [calls (volatile! 0)
            boom! (fn [_]
                    (vswap! calls inc)
                    (throw (ex-info "Boom!" {})))]
        (is (p/thrown? (run! boom! (range 2))))
        (is (= 1 @calls))))

    ;; phel implements run! in terms of doseq (other dialects use reduce)
    (testing "Terminates on Reduced"
      (let [calls (volatile! 0)
            done! (fn [_]
                    (vswap! calls inc)
                    (reduced :done))]
        (is (nil? (run! done! (range 2))))
        (is (= 1 @calls))))))
