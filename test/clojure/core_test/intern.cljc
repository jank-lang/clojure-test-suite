(ns clojure.core-test.intern
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/intern
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
   (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (intern 'unknown-namespace 'x)))
   (is (thrown? #?(:cljs :default :clj Exception :cljr Exception) (intern 'unknonw-namespace 'x 42)))))
