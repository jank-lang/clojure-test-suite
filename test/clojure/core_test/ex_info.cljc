(ns clojure.core-test.ex-info
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/ex-info

  ;; DOCSTRING:
  ;; clojure.core/ex-info
  ;; [msg map]
  ;; [msg map cause]
  ;; Create an instance of ExceptionInfo, a RuntimeException subclass
  ;; that carries a map of additional data.

  (deftest test-ex-info
    (testing "message"
      (is (= "Boom!" (ex-message (ex-info "Boom!" {})))))

    (testing "data"
      (are [data] (= data (ex-data (ex-info "Boom!" data)))
        {}
        {:a 1}
        {:a 1 :b "two" :c [1 2 3]}
        {:a nil}
        {nil nil})
      ;; `nil` is treated as an empty map; CLJS doesn't coerce it to {}
      #?(:cljs (is (nil? (ex-data (ex-info "Boom!" nil))))
         :default (is (= {} (ex-data (ex-info "Boom!" nil)))))
      ;; the metadata of the data map is preserved
      (is (= {:m true} (meta (ex-data (ex-info "Boom!" (with-meta {:a 1} {:m true})))))))

    (testing "message and data are both carried"
      (let [e (ex-info "Boom!" {:a 1 :b 2})]
        (is (= "Boom!" (ex-message e)))
        (is (= {:a 1 :b 2} (ex-data e)))))

    (testing "nil message"
      #?(:cljr nil ; CLR synthesizes a message when the message is nil
         :default (is (nil? (ex-message (ex-info nil {:a 1})))))
      (is (= {:a 1} (ex-data (ex-info nil {:a 1})))))

    (testing "cause"
      (let [cause (ex-info "Root cause" {:root true})
            e (ex-info "Wrapper" {:wrapper true} cause)]
        (is (identical? cause (ex-cause e)))
        (is (= "Wrapper" (ex-message e)))
        (is (= {:wrapper true} (ex-data e)))
        (is (= "Root cause" (ex-message (ex-cause e))))
        (is (= {:root true} (ex-data (ex-cause e)))))
      (is (nil? (ex-cause (ex-info "Boom!" {}))))
      (is (nil? (ex-cause (ex-info "Boom!" {} nil)))))

    (testing "can be thrown"
      (is (= ["Boom!" {:a 1}]
             (try
               (throw (ex-info "Boom!" {:a 1}))
               (catch #?(:cljs :default
                         :cljr System.Exception
                         :clj Exception
                         :bb Exception) e
                 [(ex-message e) (ex-data e)])))))))
