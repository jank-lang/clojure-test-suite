(ns clojure.core-test.parse-boolean
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/parse-boolean
  (deftest test-parse-boolean
    (testing "common"
      (are [expected x] (= expected (parse-boolean x))
           false "false"
           true  "true"
           nil   ""
           nil   "foo"
           nil   "False"
           nil   "FALSE"
           nil   "True"
           nil   "TRUE"))
    
    (testing "exceptions"
      #?(:clj (are [x] (thrown? Exception (parse-boolean x))
                   nil
                   0
                   0.0
                   \a 
                   :key
                   {}
                   '()
                   #{}
                   []))
      #?(:cljs (are [x] (thrown? js/Error (parse-boolean x))
                   nil
                   0
                   0.0
                   :key
                   {}
                   '()
                   #{}
                   [])))))
