(ns clojure.core-test.reduce
  (:require
   [clojure.test :as t :refer [deftest testing is are]]
   [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]])
  #?(:clj (:import (clojure.lang IReduce))))

(def interop
  {:int-new (fn [x]
              (#?(:clj Integer.
			      :cljr identity
                  :cljs js/Number.) x))

   :Integer #?(:clj Integer/TYPE
               :cljr System.Int32
               :cljs js/Number)

   :Long #?(:clj Long/TYPE
            :cljr System.Int64
            :cljs js/Number)

   :Float #?(:clj Long/TYPE
             :cljr System.Single
             :cljs js/Number)

   :Double #?(:clj Double/TYPE
              :cljr System.Double
              :cljs js/Number)

   :Boolean #?(:clj Boolean/TYPE
               :cljr System.Boolean
               :cljs js/Boolean)})


(when-var-exists clojure.core/reduce
  (deftest test-reduce
    (testing "common"
      (is (nil? (reduce nil nil nil)))
      (is (thrown? #?(:clj Exception
	                  :cljr Exception
                      :cljs js/Error) (reduce nil nil)))
      (is (= 6 (reduce + 0 [1 2 3]))))

    (testing "val is not supplied"
      (is (= 3 (reduce (fn [a b]
                         (+ a b))
                       [1 2])))

      (testing "empty coll"
        (is (= 1 (reduce (fn [] 1) []))))

      (testing "coll with 1 item"
        (is (= 1 (reduce (fn []
                           (is false)
                           (throw (ex-info "should not get here" {})))
                         [1])))))

    (testing "val is supplied, empty coll"
      (is (= 1 (reduce (fn []
                           (is false)
                         (throw (ex-info "should not get here" {})))
                       1
                       []))))

    (testing "reduction by type"
      (let [int-new (interop :int-new)
            char-new (interop :char-new)
            byte-new (interop :byte-new)
            arange (range 1 100) ;; enough to cross nodes
            avec (into [] arange)
            alist (into () arange)
            obj-array (into-array arange)
            int-array (into-array (:Integer interop) (map #(int-new (int %)) arange))
            long-array (into-array (:Long interop) arange)
            float-array (into-array (:Float interop) arange)
            double-array (into-array (:Double interop) arange)
            all-true (into-array (:Boolean interop) (repeat 10 true))]

        (testing "val is not supplied"
          (is (== 4950
                  (reduce + arange)
                  (reduce + avec)
                  #?(:bb 4950
                     :clj (.reduce ^IReduce avec +))
                  (reduce + alist)
                  (reduce + obj-array)
                  (reduce + int-array)
                  (reduce + long-array)
                  (reduce + float-array)
                  (reduce + double-array))))

        (testing "val is supplied"
          (is (== 4951
                  (reduce + 1 arange)
                  (reduce + 1 avec)
                  #?(:bb 4951
                     :clj (.reduce ^IReduce avec + 1))
                  (reduce + 1 alist)
                  (reduce + 1 obj-array)
                  (reduce + 1 int-array)
                  (reduce + 1 long-array)
                  (reduce + 1 float-array)
                  (reduce + 1 double-array))))

        (is (= true
               (reduce #(and %1 %2) all-true)
               (reduce #(and %1 %2) true all-true)))))))
