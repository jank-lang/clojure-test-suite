(ns clojure.core-test.swap-bang
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/swap!

  ;; DOCSTRING:
  ;; clojure.core/swap!
  ;; [atom f]
  ;; [atom f x]
  ;; [atom f x y]
  ;; [atom f x y & args]
  ;; Atomically swaps the value of atom to be:
  ;; (apply f current-value-of-atom args). Note that f may be called
  ;; multiple times, and thus should be free of side effects.  Returns
  ;; the value that was swapped in.

  (deftest test-swap!
    (testing "[atom f]"
      (let [atm (atom 0)]
        (is (= 1 (swap! atm inc)))
        (is (= 1 @atm))
        ;; the value returned is the value swapped in, not the old value
        (is (= 1 (swap! atm identity)))
        (is (= 1 @atm))
        (is (= 2 (swap! atm inc)))
        (is (= 2 @atm))))

    (testing "[atom f x]"
      (let [atm (atom 1)]
        (is (= 11 (swap! atm + 10)))
        (is (= 11 @atm)))
      (testing "f is called as (f current-value-of-atom x)"
        (let [atm (atom :cur)]
          (is (= [:cur :x] (swap! atm (fn [cur x] [cur x]) :x)))
          (is (= [:cur :x] @atm)))))

    (testing "[atom f x y]"
      (let [atm (atom 1)]
        (is (= 111 (swap! atm + 10 100)))
        (is (= 111 @atm)))
      (testing "f is called as (f current-value-of-atom x y)"
        (let [atm (atom :cur)]
          (is (= [:cur :x :y] (swap! atm (fn [cur x y] [cur x y]) :x :y)))
          (is (= [:cur :x :y] @atm)))))

    (testing "[atom f x y & args]"
      (let [atm (atom 1)]
        (is (= 16 (swap! atm + 1 2 3 4 5)))
        (is (= 16 @atm)))
      (testing "f is called as (apply f current-value-of-atom x y args)"
        (let [atm (atom :cur)]
          (is (= [:cur 1 2 3 4]
                 (swap! atm (fn [cur & args] (into [cur] args)) 1 2 3 4)))
          (is (= [:cur 1 2 3 4] @atm)))
        (testing "a non-commutative f shows the argument order"
          (let [atm (atom 10)]
            (is (= 1 (swap! atm - 5 4)))
            (is (= 1 @atm))))))

    ;; `swap!` stores whatever `f` returns: `nil` and `false` are ordinary
    ;; values, not a signal that the atom should be left alone.
    (testing "nil and false are ordinary values"
      (let [atm (atom nil)]
        (is (= [nil :x] (swap! atm (fn [cur x] [cur x]) :x))
            "f receives nil as the current value")
        (is (false? (swap! atm (constantly false))))
        (is (false? @atm))))

    ;; The everyday use of `swap!`: an atom holding program state as a map,
    ;; updated by successive swaps, each of which sees the value left by the
    ;; previous one.
    (testing "a map of program state is threaded through successive swaps"
      (let [atm (atom {})]
        (is (= {:x :foo} (swap! atm assoc :x :foo)))
        (is (= {:x :foo, :a {:b 1}} (swap! atm assoc :a {:b 1})))
        (is (= {:x :foo, :a {:b 2}} (swap! atm update-in [:a :b] inc)))
        (is (= {:x :foo, :a {:b 5}} (swap! atm update-in [:a :b] + 3)))
        (is (= {:x :foo, :a {:b 5}} @atm))))

    (testing "infinite sequence values are not realized"
      (let [atm (atom (range))]
        (is (= (range 3) (take 3 (swap! atm identity))))
        (is (= (range 3) (take 3 @atm)))
        (is (= [5 6 7] (take 3 (swap! atm (fn [s] (drop 5 s))))))
        (is (= [5 6 7] (take 3 @atm)))))

    ;; The docstring says: "Note that f may be called multiple times, and
    ;; thus should be free of side effects." That is an observable
    ;; consequence of swap! being atomic: if the atom changes between
    ;; reading its value and storing the new value, f is called again with
    ;; the updated value.
    (let [atm (atom 0)
          calls (atom 0)
          armed? (volatile! true)
          f (fn [cur]
              (swap! calls inc)
              (if @armed?
                ;; first call: change the atom behind swap!'s back so that
                ;; storing the new value fails, forcing a retry
                (do (vreset! armed? false)
                    (reset! atm 100) ; note that we're changing this ourselves in the `swap!`
                    :stale)
                (inc cur)))]
      #?(:cljs
         ;; CLJS is single-threaded, so nothing else can change the atom
         ;; while `swap!` runs, and there is never a race to retry for. Its
         ;; `swap!` is `(reset! a (f (deref a)))`: `f` is computed once and
         ;; its result is stored unconditionally, so correcting the atom
         ;; from inside `f` goes unnoticed, and the value that lands in the
         ;; atom is the one computed from the value `f` was handed.
         (testing "single-threaded CLJS swap! applies f exactly once"
           (is (= :stale (swap! atm f)))
           (is (= :stale @atm))
           (is (= 1 @calls)))
         :default
         (testing "f is called again when the atom changes underneath swap!"
           (is (= 101 (swap! atm f)))
           (is (= 101 @atm))
           (is (= 2 @calls) "f was called twice"))))

    (testing "an exception in f propagates and leaves the atom unchanged"
      (let [atm (atom 1)
            boom (fn [& _] (throw (ex-info "boom" {:reason :boom})))]
        ;; all four arities of swap!
        (is (p/thrown? (swap! atm boom)))
        (is (p/thrown? (swap! atm boom :x)))
        (is (p/thrown? (swap! atm boom :x :y)))
        (is (p/thrown? (swap! atm boom :x :y :z :w)))
        (is (= 1 @atm))
        ;; the atom is still usable afterwards
        (is (= 2 (swap! atm inc)))
        (is (= 2 @atm))))

    (testing "a rejected new value from the atom's validator is not stored"
      (let [atm (atom 0 :validator even?)]
        (is (= 2 (swap! atm + 2)))
        (is (= 2 @atm))
        (is (p/thrown? (swap! atm inc)))
        (is (= 2 @atm))))

    (testing "what happens when the input is not an atom?"
      (is (p/thrown? (swap! nil inc)))
      (is (p/thrown? (swap! 42 inc)))
      (is (p/thrown? (swap! "not-an-atom" inc))))))
