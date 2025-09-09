(ns clojure.core-test.fn-qmark
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(defn foo [x] (str "hello " x))

(when-var-exists clojure.core/fn?
  (deftest test-fn?
    (testing "`fn?`"
      (testing "functions, functions from HOFs, transducers, #() reader macro, `fn`, `defn`"
        (is (fn? juxt))
        (is (fn? (juxt inc dec)))
        (is (fn? (map inc)))
        (is (fn? #(str "hello " %)))
        (is (fn? (fn [x] (str "hello " x))))
        (is (fn? foo)))

      ;; Note: we intentionally do not test multimethods or protocols, because
      ;; behavior differs across dialects due to implementation decisions that
      ;; don't matter for our purposes.

      (testing "atomic values are not functions"
        (is (not (fn? nil)))
        (is (not (fn? 12345678)))
        (is (not (fn? "string")))
        (is (not (fn? :keyword))) ; note: also IFn
        (is (not (fn? 'symbol)))
        (is (not (fn? \space))))

      (testing "implementing IFn is not the same as implementing Fn"
        (is (not (fn? {:a :b})))
        (is (not (fn? #{:a :b :c})))
        (is (not (fn? [:a :b])))))))
