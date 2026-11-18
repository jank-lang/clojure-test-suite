(ns clojure.core-test.with-open
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/with-open

  ;; DOCSTRING:
  ;; clojure.core/with-open
  ;; [bindings & body]
  ;; bindings => [name init ...]
  ;;
  ;; Evaluates body in a try expression with names bound to the values
  ;; of the inits, and a finally clause that calls (.close name) on each
  ;; name in reverse order.

  ;; A `with-open` resource is anything with a close method: `java.io.Closeable`
  ;; on the JVM, `System.IDisposable` on ClojureCLR.
  (defn tracking-closeable
    "Returns an object which conjes `id` onto `log` when it is closed."
    [log id]
    #?(:cljr (reify System.IDisposable
               (Dispose [_] (swap! log conj id)))
       :clj  (reify java.io.Closeable
               (close [_] (swap! log conj id)))))

  (deftest test-with-open
    (testing "returns the value of the last expression in the body"
      (is (= :ret (with-open [x (tracking-closeable (atom []) :x)] :ret)))
      (is (= :ret (with-open [x (tracking-closeable (atom []) :x)] :ignored :ret)))
      ;; a body of no forms evaluates to nil
      (is (nil? (with-open [x (tracking-closeable (atom []) :x)])))
      ;; no resources at all
      (is (= :ret (with-open [] :ret))))

    (testing "the resource is closed after the body is evaluated"
      (let [log (atom [])]
        (is (= :ret (with-open [x (tracking-closeable log :close)]
                      (swap! log conj :body)
                      :ret)))
        (is (= [:body :close] @log))))

    (testing "the resource is closed before a lazy body result is realized"
      (let [log (atom [])
            ;; realizing the seq logs each element, so that realization is
            ;; observable and cannot happen before the resource is closed
            result (with-open [x (tracking-closeable log :close)]
                     (map (fn [n] (swap! log conj n) (inc n)) [1 2 3]))]
        ;; the body's result is not realized while the resource is still open
        (is (= [:close] @log))
        ;; the closed resource does not make the still-lazy result unusable
        (is (= [2 3 4] result))
        (is (= [:close 1 2 3] @log))))

    (testing "multiple resources are closed in reverse order of binding"
      (let [log (atom [])]
        (with-open [a (do (swap! log conj :init-a) (tracking-closeable log :close-a))
                    b (do (swap! log conj :init-b) (tracking-closeable log :close-b))
                    c (do (swap! log conj :init-c) (tracking-closeable log :close-c))]
          (swap! log conj :body))
        (is (= [:init-a :init-b :init-c :body :close-c :close-b :close-a] @log))))

    (testing "names are bound to the values of the inits within the body"
      (let [log (atom [])
            r1 (tracking-closeable log :r1)
            r2 (tracking-closeable log :r2)]
        (is (true? (with-open [a r1 b r2]
                     (and (identical? a r1) (identical? b r2)))))
        (is (= [:r2 :r1] @log))))

    (testing "the resource is closed if the body throws"
      (let [log (atom [])]
        (is (p/thrown? (with-open [x (tracking-closeable log :close)]
                         (throw (ex-info "boom" {})))))
        (is (= [:close] @log))))

    (testing "all resources are closed in reverse order if the body throws"
      (let [log (atom [])]
        (is (p/thrown? (with-open [a (tracking-closeable log :a)
                                   b (tracking-closeable log :b)
                                   c (tracking-closeable log :c)]
                         (throw (ex-info "boom" {})))))
        (is (= [:c :b :a] @log))))

    (testing "earlier resources are closed when a later init throws"
      (let [log (atom [])]
        (is (p/thrown? (with-open [a (tracking-closeable log :a)
                                   b (throw (ex-info "boom" {}))]
                         :unreachable)))
        (is (= [:a] @log))))

    (testing "nested with-open closes each level, innermost first"
      (let [log (atom [])]
        (is (= :ret (with-open [outer (tracking-closeable log :close-outer)]
                      (with-open [inner (tracking-closeable log :close-inner)]
                        (swap! log conj :body)
                        :ret))))
        (is (= [:body :close-inner :close-outer] @log)))
      (let [log (atom [])]
        (is (= :ret (with-open [a (tracking-closeable log :close-a)]
                      (with-open [b (tracking-closeable log :close-b)]
                        (with-open [c (tracking-closeable log :close-c)]
                          (swap! log conj :body)
                          :ret)))))
        (is (= [:body :close-c :close-b :close-a] @log))))

    (testing "an exception propagates out through every nested level"
      (let [log (atom [])]
        (is (p/thrown? (with-open [outer (tracking-closeable log :close-outer)]
                         (with-open [inner (tracking-closeable log :close-inner)]
                           (throw (ex-info "boom" {}))))))
        (is (= [:close-inner :close-outer] @log)))
      (let [log (atom [])]
        (is (p/thrown? (with-open [a (tracking-closeable log :close-a)]
                         (with-open [b (tracking-closeable log :close-b)]
                           (with-open [c (tracking-closeable log :close-c)]
                             (throw (ex-info "boom" {})))))))
        (is (= [:close-c :close-b :close-a] @log))))

    (testing "a with-open in the binding form closes the resource it opened"
      ;; the init of `inner` throws, so `inner` is never bound; the resource
      ;; opened inside the init is closed, and so is the already-bound `outer`
      (let [log (atom [])]
        (is (p/thrown? (with-open [outer (tracking-closeable log :close-outer)]
                         (with-open [inner (with-open [x (tracking-closeable log :close-init)]
                                             (throw (ex-info "boom" {})))]
                           :unreachable))))
        (is (= [:close-init :close-outer] @log)))
      ;; when the init of the only binding throws, no name is bound and so no
      ;; name is closed; only the resource opened inside the init is closed
      (let [log (atom [])]
        (is (p/thrown? (with-open [a (with-open [x (tracking-closeable log :close-init)]
                                       (throw (ex-info "boom" {})))]
                         :unreachable)))
        (is (= [:close-init] @log))))))
