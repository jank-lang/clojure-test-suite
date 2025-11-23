(ns clojure.core-test.parse-uuid
  (:require clojure.core
            [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists parse-uuid
  (deftest test-parse-uuid
    (testing "common"
      (are [expected x] (= expected (parse-uuid x))
           nil ""
           nil "0"
           nil "df0993"
           nil "b6883c0a-0342-4007-9966-bc2dfa6b109eb"
           nil "ab6883c0a-0342-4007-9966-bc2dfa6b109e")
      (is (= (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e") (parse-uuid "B6883C0A-0342-4007-9966-BC2dfa6b109E")))
      (when-var-exists instance?
        #?(:clj  (is (instance? java.util.UUID (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e"))))
        #?(:cljs (is (instance? cljs.core.UUID (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e"))))))
    (testing "tolerance to non-standard forms"
      (are [expected s] (= #?(:clj expected :default nil) (parse-uuid s)) ; clj is permissive, others are strict
                        #uuid "00000000-0000-0000-0000-000000000000" "0-0-0-0-0"
                        #uuid "00000012-0034-0056-0078-000000000009" "12-34-56-78-9"
                        #uuid "00000005-0004-0003-0002-009000000001" "5-4-3-DEADBEEF0002-9000000001"))
    (testing "exceptions"
      #?(:cljs (are [x] (thrown? js/Error (parse-uuid x))
                   {}
                   '()
                   []
                   #{}
                   :key
                   0.0
                   1000)
         :default (are [x] (thrown? Exception (parse-uuid x))
                   {}
                   '()
                   []
                   #{}
                   \a
                   :key
                   0.0
                   1000)))))
