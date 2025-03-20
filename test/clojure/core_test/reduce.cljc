(ns clojure.core-test.reduce
  (:require
   [clojure.test :as t :refer [deftest testing is are]]
   [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]])
  #?(:clj (:import (clojure.lang IReduce))))

(def interop
  {:int-new (fn [x]
              (#?(:clj Integer.
                  :cljs js/Number.) x))

   :Integer #?(:clj Integer/TYPE
               :cljs js/Number)

   :Long #?(:clj Long/TYPE
            :cljs js/Number)

   :Float #?(:clj Long/TYPE
             :cljs js/Number)

   :Double #?(:clj Double/TYPE
              :cljs js/Number)

   :Boolean #?(:clj Boolean/TYPE
               :cljs js/Boolean)})


(when-var-exists clojure.core/reduce
  (deftest test-reduce
    (testing "common"
      (is (nil? (reduce nil nil nil)))
      (is (thrown? #?(:clj Exception
                      :cljs js/Error) (reduce nil nil)))
      (is (= 6 (reduce + 0 [1 2 3]))))

    (testing "edge cases"
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
        (is (== 4950
                (reduce + arange)
                (reduce + avec)
                #?(:clj (.reduce ^IReduce avec +))
                (reduce + alist)
                (reduce + obj-array)
                (reduce + int-array)
                (reduce + long-array)
                (reduce + float-array)
                (reduce + double-array)))

        (is (== 4951
                (reduce + 1 arange)
                (reduce + 1 avec)
                #?(:clj (.reduce ^IReduce avec + 1))
                (reduce + 1 alist)
                (reduce + 1 obj-array)
                (reduce + 1 int-array)
                (reduce + 1 long-array)
                (reduce + 1 float-array)
                (reduce + 1 double-array)))

        (is (= true
               (reduce #(and %1 %2) all-true)
               (reduce #(and %1 %2) true all-true)))))))
