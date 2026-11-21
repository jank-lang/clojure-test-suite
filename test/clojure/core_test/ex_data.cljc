(ns clojure.core-test.ex-data
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/ex-data

  ;; DOCSTRING:
  ;; clojure.core/ex-data
  ;; [ex]
  ;; Returns exception data (a map) if ex is an IExceptionInfo.
  ;; Otherwise returns nil.

  (deftest test-ex-data
    ;; ex-data returns the map carried by an IExceptionInfo and nil for
    ;; anything else. A freshly created, a thrown and caught, and a
    ;; caused ExceptionInfo are exercised below, then non-exceptions.
    (testing "returns the data carried by an ExceptionInfo"
      (are [data] (= data (ex-data (ex-info "Boom!" data)))
        {}
        {:a 1}
        {:a 1 :b "two" :c [1 2 3]}
        {:a nil}
        {nil nil}
        {:a {:b {:c 3}}}))

    (testing "returns the data of an ExceptionInfo that has been thrown"
      (let [data {:a 1 :b "two"}]
        (is (= data (try
                      (throw (ex-info "Boom!" data))
                      (catch #?(:cljs :default :default Exception) e
                        (ex-data e)))))))

    (testing "returns the data of that ExceptionInfo only"
      ;; the cause carries its own data, which is not merged in
      (let [cause (ex-info "Root cause" {:root true})
            e     (ex-info "Wrapper" {:wrapper true} cause)]
        (is (= {:wrapper true} (ex-data e)))
        (is (= {:root true} (ex-data (ex-cause e))))))

    (testing "returns nil if ex is not an IExceptionInfo"
      (are [x] (nil? (ex-data x))
        nil
        true
        0
        3.14
        "a string"
        :a-keyword
        'a-symbol
        []
        '(1 2 3)
        {:a 1}
        #{1 2 3}
        (fn [x] x)))))
