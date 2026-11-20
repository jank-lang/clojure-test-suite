(ns clojure.core-test.realized-qmark
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

;; You realize the sun doesn't go down
;; It's just an illusion caused by the world spinning 'round
;; Do you realize?
;;
;; --The Flaming Lips

(when-var-exists realized?
  (deftest test-realized?
    (testing "`realized?`"

      ;;; Common cases

      (testing "What happens when the input is nil?"
        (is (p/thrown? (realized? nil))))

      (testing "What happens if it's given all valid inputs?"
        ;; per docstring: "promise, delay, future or lazy sequence"

        (when-var-exists promise
          (testing "promises"
            (let [prms (promise)]
              (is (false? (realized? prms)))
              (deliver prms :foo)
              (is (true? (realized? prms))))))

        (testing "delays with force"
          (let [dly-f (delay :force-me)]
            (is (false? (realized? dly-f)))
            (force dly-f)
            (is (true? (realized? dly-f)))))
        (testing "delays with deref"
          (let [dly-d (delay :deref-me)]
            (is (false? (realized? dly-d)))
            (deref dly-d)
            (is (true? (realized? dly-d)))))

        (when-var-exists future
          (testing "futures"
            (let [ftr (future :foo)]
              (deref ftr)
              (is (true? (realized? ftr))))
            ;; Gated like `ftr3` below, so the "not yet realized" state
            ;; cannot be missed even on a loaded machine. Cancelling leaves
            ;; the parked body alone, so release the gate afterwards to let
            ;; its thread exit.
            (let [gate (promise)
                  ftr2 (future @gate)]
              (is (false? (realized? ftr2)))
              (future-cancel ftr2)
              (is (true? (realized? ftr2)))
              (deliver gate :done))
            ;; Gate the future's body on an undelivered promise so that it
            ;; cannot possibly finish before we check. A fixed sleep would
            ;; race the main thread and flake on a loaded machine.
            (let [gate (promise)
                  ftr3 (future @gate)]
              (is (false? (realized? ftr3)))
              (deliver gate :done)
              (deref ftr3)                  ; barrier: the body has now returned
              (is (true? (realized? ftr3))))))

        (testing "lazy sequences"
          (is (false? (realized? (lazy-seq))))
          (is (true? (realized? (doall (lazy-seq)))))
          (let [;; From https://clojuredocs.org/clojure.core/lazy-seq#example-54d152d7e4b0e2ac61831cfc
                square (fn square [n] (* n n))
                squares (fn squares [n]
                          ;; please handle with care: avoid e.g. `doall` into infinity
                          (lazy-seq (cons (square n) (squares (inc n)))))
                ;; Note, `square` is NOT called yet in the following line
                our-squares (squares 1)]
            (is (false? (realized? our-squares)))
            (is (empty? (take 0 our-squares)))
            (is (false? (realized? our-squares)))
            (is (= 1 (count (take 1 our-squares))))
            (is (true? (realized? our-squares))))))

      (testing "Special case inputs"
        ;; the deref'd value is not a valid input
        (when-var-exists delay
          (is (p/thrown? (realized? (deref (delay :delay))))))
        (when-var-exists future
          (is (p/thrown? (realized? (deref (future :future)))))))

      ;;; Edge cases

      (testing "What happens when the input is an incorrect shape?"
        (is (p/thrown? (realized? 1)))
        (is (p/thrown? (realized? :foo)))
        (is (p/thrown? (realized? "foo")))
        (is (p/thrown? (realized? \f)))
        (is (p/thrown? (realized? 'foo)))
        (is (p/thrown? (realized? ##NaN)))

        (is (p/thrown? (realized? '())))
        (is (p/thrown? (realized? '(:foo :bar :baz))))
        (is (p/thrown? (realized? [])))
        (is (p/thrown? (realized? {})))
        (is (p/thrown? (realized? #{})))))))
