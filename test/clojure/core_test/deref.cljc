(ns clojure.core-test.deref
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists sleep] :as p]))

(when-var-exists clojure.core/deref

  ;; DOCSTRING:
  ;; clojure.core/deref
  ;; [ref]
  ;; [ref timeout-ms timeout-val]
  ;; Also reader macro: @ref/@agent/@var/@atom/@delay/@future/@promise. Within a transaction,
  ;; returns the in-transaction-value of ref, else returns the
  ;; most-recently-committed value of ref. When applied to a var, agent
  ;; or atom, returns its current state. When applied to a delay, forces
  ;; it if not already forced. When applied to a future, will block if
  ;; computation not complete. When applied to a promise, will block
  ;; until a value is delivered.  The variant taking a timeout can be
  ;; used for blocking references (futures and promises), and will return
  ;; timeout-val if the timeout (in milliseconds) is reached before a
  ;; value is available. See also - realized?.

  (deftest test-deref
    (testing "atom"
      (is (nil? (deref (atom nil))))
      (let [atm (atom :current)]
        (is (= :current (deref atm)))
        (is (= :current @atm) "the reader macro is equivalent to deref")
        (reset! atm :updated)
        (is (= :updated (deref atm)) "returns the current state, not a snapshot")))

    ;; CLJS has neither agents nor refs.
    #?@(:cljs []
        :default
        [(testing "agent"
           (is (nil? (deref (agent nil))))
           (is (= :current (deref (agent :current))))
           (let [agnt (agent :current)]
             (is (= :current (deref agnt)))
             (is (= :current @agnt))
             (send agnt (constantly :updated))
             (await agnt)
             (is (= :updated (deref agnt)))))

         (testing "ref"
           (is (nil? (deref (ref nil))))
           (is (= :current (deref (ref :current))))
           (let [r (ref :current)]
             (is (= :current (deref r)))
             (is (= :current @r))
             (dosync (ref-set r :committed))
             (is (= :committed (deref r)) "returns the most-recently-committed value")
             (dosync
              (is (= :committed (deref r)))
              (alter r name)
              (is (= "committed" (deref r)) "returns the in-transaction-value"))
             (is (= "committed" (deref r)))))]) 

    (testing "var"
      (is (= clojure.core/deref @#'clojure.core/deref)
          "deref of a var is the var's current value")
      (is (fn? @#'clojure.core/deref))
      (is (ifn? (deref #'clojure.core/deref))))

    (testing "delay"
      (let [dly (delay :forced)]
        (is (= :forced (deref dly)))
        (is (= :forced (deref dly))))
      (testing "is only forced once"
        (let [forced (atom 0)
              dly (delay (swap! forced inc))]
          (is (= 1 (deref dly)))
          (is (= 1 (deref dly)))
          (is (= 1 (deref dly)))
          (is (= 1 (deref forced)) "the body is not re-run by later derefs")))
      (testing "caches nil and false, so the body still runs only once"
        (are [v] (let [forced (atom 0)
                       dly (delay (swap! forced inc) v)]
                   (and (= v (deref dly))
                        (= v (deref dly))
                        (= 1 (deref forced))))
          nil
          false)))

    ;; CLJS has neither `future` nor `promise`.
    #?@(:cljs []
        :default
        [(testing "future"
           (is (= :done (deref (future :done))))
           (is (= :done @(future :done)))
           (is (= :done (deref (future (sleep 10) :done)))
               "blocks until the computation is complete"))

         (testing "promise"
           (let [prms (promise)]
             (deliver prms :delivered)
             (is (= :delivered (deref prms)))
             (is (= :delivered @prms)))
           (is (nil? (deref (doto (promise) (deliver nil))))
               "a delivered nil is a value, not an absence of one"))])

    (testing "nesting deref"
      (let [atm (atom (atom :inner))]
        (is (= :inner (deref (deref atm))))
        (is (= :inner @@atm)))
      (let [atm (atom (delay :inner))]
        (is (= :inner (deref (deref atm))))
        (is (= :inner (deref (deref atm))) "the inner delay is only forced once")))

    (testing "nil"
      (is (p/thrown? (deref nil))))

    (testing "non-references"
      (are [x] (p/thrown? (deref x))
        1
        "string"
        :keyword
        'symbol
        \c
        '(1 2 3)
        [1 2 3]
        {:k "v"}
        #{1 2 3}
        (fn [x] x)))

    ;; CLJS's `deref` only has the 1-arity, so it has no timeout variant,
    ;; no timeout to test, and no blocking references to time out.
    #?@(:cljs []
        :default
        [(testing "the timeout is not reached, so the value itself is returned"
           (is (= :done (deref (future :done) 10000 :timed-out)))
           (is (= :done (deref (future (sleep 50) :done) 10000 :timed-out)))
           (is (= :delivered (deref (doto (promise) (deliver :delivered))
                                    10000 :timed-out)))
           (let [prms (promise)]
             (future (sleep 20) (deliver prms :finally-delivered))
             (is (= :finally-delivered (deref prms 10000 :timed-out))
                 "waits for the value instead of falling back to timeout-val")))

         (testing "the timeout is reached, so timeout-val is returned"
           (is (= :timed-out (deref (future (sleep 10000)) 50 :timed-out))
               "returns timeout-val rather than blocking for 10s")
           (is (= :timed-out (deref (promise) 50 :timed-out))))

         (testing "non-references with a timeout"
           (are [x] (p/thrown? (deref x 1000 :timed-out))
             nil
             1
             [1 2 3]))])))
