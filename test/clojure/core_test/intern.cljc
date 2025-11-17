(ns clojure.core-test.intern
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists intern
 (deftest test-intern
   ;; Intern and bind
   (let [x-var (intern 'clojure.core-test.intern 'x 42)]
     (is (= 42 (var-get x-var))))

   ;; Use intern to return the previously interned var
   (let [x-var (intern 'clojure.core-test.intern 'x)]
     (is (= 42 (var-get x-var))))

   ;; Create new namespace and use that as argument to intern
   (let [n (create-ns 'avoid-a-clash)
         x-var (intern n 'x 42)]
     (is (= 42 (var-get x-var))))

   (let [x-var (intern 'avoid-a-clash 'x)]
     (is (= 42 (var-get x-var))))

   ;; Trying to intern to an unknown namespace should throw
   (is (thrown? #?(:cljs :default :default Exception) (intern 'unknown-namespace 'x)))
   (is (thrown? #?(:cljs :default :default Exception) (intern 'unknown-namespace 'x 42)))))
