(ns clojure.core-test.binding
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists binding
  (def  ^:dynamic *x* :unset)
  (def  ^:dynamic *y* nil)
  (defn ^:dynamic *f* [x] (inc x))

  (defn test-fn [] *x*)

  (deftest test-binding
    ;; base-case with no overrides
    (is (= *x* :unset) "Unset is :unset")
    (is (= (*f* 1) 2)  "fn call")

    ;; common cases
    (is (binding [*x* :set] (= *x*       :set)) "Can bind dynamic var.")
    (is (binding [*x* :set] (= (test-fn) :set)) "Binding for indirect reference.")
    (is (binding [*x* nil]  (= (test-fn) nil))  "Dynamic vars are nullable.")
    (is (binding [*f* dec]  (= (*f* 1)   0))    "Can bind functions.")

    ;; infinite seqs
    (binding [*x* (range)]
      (is (= '(0 1 2 3) (take 4 (test-fn))) "Infinite range")
      (is (= '(0 1 2 3) (take 4 (test-fn))) "Immutability"))

    ;; Nested cases
    (binding [*x* :first!]
      (let [layer-1 (fn [] (test-fn))]
        (binding [*x* :second!]
          (is (= :second! (layer-1) (test-fn)) "Value is determined at call-site"))))
    (binding [*y* *x*]
      (is (= *y* :unset) "Dynamic reference is by value at binding.")
      (binding [*x* :layer-2]
        (is (= *y* :unset) "Dynamic reference does not update."))
      (binding [*y* *x*
                *x* :set-later]
        (is (= *y* :unset) "Bind vars are applied in sequence.")))
    (let [f (fn [] (binding [*x* :inside-f] (test-fn)))]
      (binding [*x* :outside-f]
        (is (= (test-fn) :outside-f))
        (is (= (f)       :inside-f) "Nested in func-call")))
    (binding [*y* (binding [*x* :bad] (test-fn))]
      (is (= *y* :bad) "Binding in a binding vector"))

    ;; Threading/future/delay cases
    (let [f (delay (test-fn))]
      (binding [*x* :here]
        (is (= @f :here) "Delayed functions inherit their bindings when forced"))
      (is (= @f :here) "And value persists outside binding expression"))

    ;; CLJS doesn't have futures
    #?@(:cljs []
        :default
        [(let [f (future (test-fn))]
           (binding [*x* :now-here]
             (is (= @f :unset) "Thread context is separate from joining thread")))
         (binding [*x* :outer]
           (let [f (future (test-fn))]
             (binding [*x* :inner]
               (is (= @f :outer) "Thread context preserves binding context."))))
         (binding [*x* :caller]
           (let [f (future
                     (binding [*x* :callee]
                       (future (test-fn))))]
             (binding [*x* :derefer]
               (let [derefed-f @f]
                 (is (= :callee @derefed-f) "Binding in futures preserved.")))))])))
