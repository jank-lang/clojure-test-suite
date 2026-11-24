(ns clojure.core-test.ex-message
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability :as p #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/ex-message

  ;; DOCSTRING:
  ;; clojure.core/ex-message
  ;; [ex]
  ;; Returns the message attached to ex if ex is a Throwable.
  ;; Otherwise returns nil.

  (deftest test-ex-message
    ;; ex-message returns the message of any Throwable and nil for
    ;; everything else. ExceptionInfo, other Throwables and non-throwables
    ;; are exercised below.
    (testing "returns the message attached to an ExceptionInfo"
      (are [msg] (= msg (ex-message (ex-info msg {})))
        "Boom!"
        ""
        "a message with spaces"
        "héllo → 世界"))

    (testing "returns the message of the exception itself, not of its cause"
      ;; the cause carries its own message, which is not substituted for this one
      (is (= "Wrapper" (ex-message (ex-info "Wrapper" {} (ex-info "Root cause" {}))))))

    (testing "returns the message of any Throwable, not just an ExceptionInfo"
      #?(:cljs (is (= "oops" (ex-message (js/Error. "oops"))))
         :cljr (is (= "oops" (ex-message (System.Exception. "oops"))))
         :default (is (= "oops" (ex-message (Exception. "oops"))))))

    (testing "returns nil if the exception has no message"
      ;; CLR synthesizes a message when the message given to `ex-info` is nil
      #?(:cljr nil
         :default (is (nil? (ex-message (ex-info nil {}))))))

    (testing "returns nil if ex is not a Throwable"
      (are [x] (nil? (ex-message x))
        nil
        true
        0
        "a string"
        :a-keyword
        []
        {:a 1}
        (fn [x] x)))))
