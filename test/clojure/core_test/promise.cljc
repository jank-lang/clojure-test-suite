(ns clojure.core-test.promise
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/promise

  ;; DOCSTRING:
  ;; clojure.core/promise
  ;; []
  ;; Returns a promise object that can be read with deref/@, and set,
  ;; once only, with deliver. Calls to deref/@ prior to delivery will
  ;; block, unless the variant of deref with timeout is used. All
  ;; subsequent derefs will return the same delivered value without
  ;; blocking. See also - realized?.

  (deftest test-promise
    (testing "a new promise holds no value"
      (is (false? (realized? (promise))) "a fresh promise is not realized"))

    (testing "@ returns the delivered value"
      (are [v] (let [prms (promise)]
                 (deliver prms v)
                 (and (= v @prms)
                      (true? (realized? prms))))
        nil
        false
        true
        0
        1.5
        \c
        "string"
        :keyword
        :ns/keyword
        'symbol
        'ns/symbol
        '(1 2 3)
        [1 2 3]
        {:k "v"}
        #{1 2 3})

      (let [prms (promise)]
        (deliver prms (range))
        (is (= [0 1 2] (take 3 @prms))
            "delivering and reading an infinite sequence does not realize it")))

    (testing "the delivered value is returned as-is"
      (let [f (fn [] :called)
            prms (promise)]
        (deliver prms f)
        (is (identical? f @prms))))

    (testing "promises are independent"
      (let [prms1 (promise)
            prms2 (promise)]
        (deliver prms1 :one)
        (is (false? (realized? prms2)) "delivering one promise delivers no other")
        (deliver prms2 :two)
        (is (= [:one :two] [@prms1 @prms2]))))

    (testing "delivery is once only"
      (let [prms (promise)]
        (deliver prms :first)
        (deliver prms :second)
        (is (= :first @prms) "a second delivery does not replace the first")
        (is (true? (realized? prms))))
      (let [prms (promise)]
        (deliver prms nil)
        (deliver prms :second)
        (is (nil? @prms) "a delivered nil is a value, so it is not replaced")))

    (testing "deref blocks until a value is delivered"
      ;; A future parked in `deref` cannot finish before the delivery, so none of
      ;; these checks can race on a loaded machine.
      (let [prms (promise)
            waiter (future @prms)]
        (is (false? (realized? waiter)) "reading an undelivered promise blocks")
        (deliver prms :released)
        (is (= :released @waiter)))

      (let [prms (promise)
            waiters (doall (repeatedly 3 #(future @prms)))]
        (deliver prms :broadcast)
        (is (= [:broadcast :broadcast :broadcast] (mapv deref waiters))
            "one delivery releases every blocked read")))

    (testing "nesting"
      ;; The value of a promise may itself be a promise; reading the outer
      ;; promise blocks on the outer promise only.
      (let [outer (promise)
            inner (promise)
            waiter (future @outer)]
        (deliver inner :inner-value)
        (deliver outer inner)
        (is (= :inner-value @@outer))
        (is (identical? inner @waiter)
            "delivering a promise releases a blocked read"))

      ;; Here the outer promise is delivered first and the inner one last, so
      ;; reading through the outer promise blocks on the inner promise.
      (let [inner (promise)
            outer (doto (promise) (deliver inner))
            waiter (future @@outer)]
        (is (false? (realized? waiter))
            "an undelivered inner promise keeps the read through it blocked")
        (deliver inner :inner-value)
        (is (= :inner-value @waiter)
            "delivering the inner promise releases the blocked read")))

    (testing "the deref timeout variant"
      ;; The timeout-reached cases come first: they are the fast ones, and a
      ;; delivery that arrives late can then only show up as a failure of the
      ;; wait-for-the-value case at the end.
      (is (= ::timed-out (deref (promise) 50 ::timed-out))
          "an undelivered promise returns timeout-val rather than blocking")
      (is (nil? (deref (promise) 50 nil)) "timeout-val may be nil")
      (let [prms (promise)]
        (is (= ::timed-out (deref prms 20 ::timed-out)))
        (is (false? (realized? prms)) "a timed-out deref does not realize the promise")
        (deliver prms :delivered-later)
        (is (= :delivered-later @prms)
            "a timed-out deref leaves the promise waiting for its value"))

      ;; The value is there before the timeout expires, so it is returned instead
      ;; of timeout-val. A promise that is already delivered is read without
      ;; consulting the clock at all, so a 0 ms timeout still returns the value:
      ;; timeout-val here could only mean the clock was checked before the value.
      (is (= :delivered
             (deref (doto (promise) (deliver :delivered)) 0 ::timed-out))
          "a delivered promise does not wait for the timeout")
      (is (nil? (deref (doto (promise) (deliver nil)) 0 ::timed-out))
          "a delivered nil is a value, not a timeout")

      ;; A timed read that has to wait for the value when it arrives. The 10s
      ;; timeout below is never raced against: the reader is held behind a gate
      ;; and prms is delivered before the gate is opened, so the value is
      ;; already there when the timed read starts, whatever the clock does.
      (let [prms (promise)
            gate (promise)
            waiter (future
                     @gate
                     (deref prms 10000 ::timed-out))]
        (deliver prms :in-time)
        (deliver gate ::go)
        (is (= :in-time @waiter)
            "a timed read returns the value rather than timeout-val")))))
