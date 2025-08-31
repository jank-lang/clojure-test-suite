(ns clojure.core-test.with-out-str
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/with-out-str
  (deftest test-with-out-str
    (is (= (str "some sample :text here" \newline
                "[:a :b] {:c :d} #{:e} (:f)" \newline)
           (with-out-str
             (println "some" "sample" :text 'here)
             (prn [:a :b] {:c :d} #{:e} '(:f)))))))
