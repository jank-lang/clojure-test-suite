(ns clojure.core-test.bound-fn-star
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(def ^:dynamic *x* :unset)

(defn test-fn [] *x*)

(when-var-exists bound-fn*
 (deftest test-bound-fn*
   (testing "base case"
     (let [f (bound-fn* test-fn)]
       (is (= (f) :unset) "picks up dynamic vars")
       (binding [*x* :set]
         (is (= (f) :set) "And tracks their changes"))))

   (testing "Common cases"
     (binding [*x* :set]
       (let [f (bound-fn* test-fn)]
         (binding [*x* :set-again]
           (is (= (f) :set) "bound-fn stores values")))))

   (testing "Infinite seqs"
     (binding [*x* (range)]
       (let [f (bound-fn* test-fn)]
         (binding [*x* (drop 50 (range))]
           (is (= (range 10)
                    (take 10 (f))
                    (take 10 (f))) "infinite seqs work")))))

   (testing "Nested cases"
     (binding [*x* :first!]
       (let [f (bound-fn* test-fn)]
         (binding [*x* :second!]
           (let [f (fn [] [(f) ((bound-fn* test-fn))])]
             (is (= (f) [:first! :second!]) "Nested bound functions work as expected.")))))
     (let [f (fn [] (binding [*x* :inside-f]
                      (bound-fn* test-fn)))]
       (binding [*x* :outside-f]
         (is (= ((f)) :inside-f) "bound-fn as result preserves initial bindings"))))

   (testing "Threaded/future cases"
     (let [f (bound-fn* test-fn)
           fut (future (f))]
       (binding [*x* :here]
         (is (= @fut :unset) "bound-fn stays bound even in other thread"))))
     (binding [*x* :caller]
       (let [f (future
                 (binding [*x* :callee]
                   (future (bound-fn* test-fn))))]
         (binding [*x* :derefer]
           (let [derefed-f @f]
             (is (= :callee (@derefed-f)) "Binding in futures preserved.")))))))
