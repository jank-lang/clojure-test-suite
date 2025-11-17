(ns clojure.core-test.fnil
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists fnil
 (defn test-fn [& x]
   (into [] x))

 (def arg 'not-nil)

 (deftest fnil-test
   (let [arity-1 (fnil test-fn 100)]
     (is (= [100]             (arity-1 nil)))
     (is (= [arg]             (arity-1 arg))))
   (let [arity-2 (fnil test-fn 100 200)]
     (is (= [100 200]         (arity-2 nil nil)))
     (is (= [arg 200]         (arity-2 arg nil)))
     (is (= [100 arg]         (arity-2 nil arg)))
     (is (= [arg arg]         (arity-2 arg arg))))
   (let [arity-3 (fnil test-fn 100 200 300)]
     (is (= [100 200 300]     (arity-3 nil nil nil)))
     (is (= [arg 200 300]     (arity-3 arg nil nil)))
     (is (= [100 arg 300]     (arity-3 nil arg nil)))
     (is (= [100 200 arg]     (arity-3 nil nil arg)))
     (is (= [arg arg 300]     (arity-3 arg arg nil)))
     (is (= [100 arg arg]     (arity-3 nil arg arg)))
     (is (= [arg 200 arg]     (arity-3 arg nil arg)))
     (is (= [arg arg arg]     (arity-3 arg arg arg))))
   (let [arity-+ (fnil test-fn 100 200 300)]
     (is (= [100 200 300 arg] (arity-+ nil nil nil arg)))
     (is (= [arg 200 300 arg] (arity-+ arg nil nil arg)))
     (is (= [100 arg 300 arg] (arity-+ nil arg nil arg)))
     (is (= [100 200 arg arg] (arity-+ nil nil arg arg))))))
