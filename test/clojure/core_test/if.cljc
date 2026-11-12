(ns clojure.core-test.if
  (:require [clojure.test :as t :refer [are deftest is testing]]))

;; Tests the semantics of `if`, the special form at the root of every
;; conditional in Clojure:
;;
;;   (if test then else?)
;;
;; - `test` is evaluated first. If its value is neither `nil` nor
;;   `false`, `then` is evaluated and its value returned; otherwise
;;   `else` is evaluated and its value returned.
;; - `nil` and `false` are the only falsey values. Everything else is
;;   truthy, including 0, 0.0, ##NaN, the empty string, the empty
;;   collections, and function values. All of the other conditionals
;;   (`and`, `or`, `when`, `cond`, ...) are based on this same logic, so
;;   `if` is the place to pin down truthiness.
;; - The branch not taken is not evaluated at all.
;; - `else` may be omitted, in which case it defaults to `nil`.
;; - The value of the selected branch is returned as-is; it is not
;;   coerced to a boolean.
;; - `recur` is permitted in tail position in either branch.
;;
;; Note that `if` is a special form and not a var, so the usual
;; `when-var-exists` guard would skip this whole file in every dialect
;; (`resolve` returns nil for special forms). The tests below are
;; therefore unguarded; `if` itself is always available.
(deftest test-if
  (testing "a truthy test selects the `then` branch"
    (is (= :then (if true :then :else)))
    (is (= :then (if (= 1 1) :then :else)))
    (is (= :then (if (< 1 2) :then :else))))

  (testing "nil and false are the only falsey values"
    (is (= :else (if nil :then :else)))
    (is (= :else (if false :then :else)))
    (is (= :else (if (= 1 2) :then :else)))
    (is (= :else (if (nil? :anything) :then :else))))

  (testing "everything else is truthy"
    (are [x] (= :then (if x :then :else))
      true
      0                  ; zero is falsey in many other languages
      1
      0.0
      1.0
      ##NaN              ; NaN is neither nil nor false
      ""                 ; the empty string
      "0"                ; nor are the strings "0" and "1" special
      "1"
      "false"
      \a
      :a-keyword
      '()                ; the empty list
      []
      [nil]
      {}
      #{}
      (constantly nil)   ; a function value is not the nil it returns
      (fn [] nil)))

  (testing "the test expression is evaluated exactly once"
    (let [calls (volatile! 0)]
      (is (= :then (if (vswap! calls inc) :then :else)))
      (is (= 1 @calls)))
    (let [calls (volatile! 0)]
      (is (= :else (if (do (vswap! calls inc) false) :then :else)))
      (is (= 1 @calls))))

  (testing "the branch not taken is not evaluated"
    (is (= :then (if true :then (/ 1 0))))
    (is (= :else (if false (/ 1 0) :else)))
    (let [calls (volatile! 0)]
      (is (= :then (if true :then (vswap! calls inc))))
      (is (= 0 @calls)))
    (let [calls (volatile! 0)]
      (is (= :else (if false (vswap! calls inc) :else)))
      (is (= 0 @calls))))

  (testing "the branch taken is evaluated exactly once"
    (let [calls (volatile! 0)]
      (is (= 1 (if true (vswap! calls inc) :else)))
      (is (= 1 @calls)))
    (let [calls (volatile! 0)]
      (is (= 1 (if false :then (vswap! calls inc))))
      (is (= 1 @calls))))

  (testing "when `else` is omitted it defaults to nil"
    (is (= :then (if true :then)))
    (is (nil? (if false :then)))
    (is (nil? (if nil :then)))
    ;; the default is nil even when the `then` branch yields false
    (is (nil? (if false false)))
    (is (nil? (if nil false))))

  (testing "the value of the selected branch is returned as-is"
    ;; no conversion of the result to a boolean
    (is (nil? (if true nil :else)))
    (is (false? (if true false :else)))
    (is (false? (if false :then false)))
    (is (= 42 (if true 42 :else)))
    (is (= [1 2 3] (if true [1 2 3] :else)))
    (is (= {:a 1} (if false :then {:a 1})))
    ;; `if` produces a value, so it works in expression position
    (is (= [:t :f :t] (map (fn [x] (if x :t :f)) [true nil 0]))))

  (testing "a branch is a single expression, so `do` groups several"
    (is (= :b (if true (do :a :b) :else)))
    (is (= :b (if false :then (do :a :b)))))

  (testing "nested `if`s"
    (is (= :small (if true (if false :big :small) :other)))
    (is (= :other (if false (if false :big :small) :other)))
    (are [x expected] (= expected (if (pos? x) :positive (if (neg? x) :negative :zero)))
      1 :positive
      -1 :negative
      0 :zero))

  (testing "branches are not forced"
    ;; truthiness of an infinite sequence doesn't realize it
    (is (= :then (if (range) :then :else)))
    (is (= 0 (first (if true (range) :else))))
    (is (= '(0 1 2) (take 3 (if true (range) :else)))))

  (testing "recur is allowed in tail position in either branch"
    (is (= 5 (loop [i 0] (if (< i 5) (recur (inc i)) i))))
    (is (= 4 (loop [i 10] (if (< i 5) i (recur (dec i))))))
    (letfn [(count-to [i] (if (< i 5) (recur (inc i)) i))]
      (is (= 5 (count-to 0))))))
