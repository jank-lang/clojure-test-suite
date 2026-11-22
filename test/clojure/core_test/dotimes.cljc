(ns clojure.core-test.dotimes
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/dotimes

  ;; DOCSTRING:
  ;; clojure.core/dotimes
  ;; [bindings & body]
  ;; bindings => name n
  ;;
  ;; Repeatedly executes body (presumably for side-effects) with name
  ;; bound to integers from 0 through n-1.

  (deftest test-dotimes
    (testing "returns nil"
      (is (nil? (dotimes [i 3] :side-effect)))
      (is (nil? (dotimes [i 3]))) ; empty body is allowed
      (is (nil? (dotimes [i 0] (throw (ex-info "never runs" {}))))))

    (testing "body runs n times with name bound to 0 through n-1"
      (are [expected n] (= expected
                            (let [seen (atom [])]
                              (dotimes [i n] (swap! seen conj i))
                              @seen))
        [] 0
        [0] 1
        [0 1 2] 3
        [0 1 2 3 4 5 6 7 8 9] 10))

    (testing "n is evaluated exactly once, before the body runs"
      (let [log (atom [])]
        (dotimes [i (do (swap! log conj :n) 2)]
          (swap! log conj i))
        (is (= [:n 0 1] @log))))

    (testing "each iteration binds a fresh value, so closures see their own index"
      (let [fs (atom [])]
        (dotimes [i 3] (swap! fs conj (fn [] i)))
        (is (= [0 1 2] (mapv #(%) @fs)))))

    (testing "nesting works, and the inner count may depend on the outer name"
      (let [pairs (atom [])]
        (dotimes [x 2]
          (dotimes [y (inc x)]
            (swap! pairs conj [x y])))
        (is (= [[0 0] [1 0] [1 1]] @pairs))))))
