(ns clojure.core-test.parse-uuid
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/parse-uuid
  (deftest test-parse-uuid
    (testing "common"
      (are [expected x] (= expected (parse-uuid x))
           nil ""
           nil "0"
           nil "df0993"
           nil "b6883c0a-0342-4007-9966-bc2dfa6b109eb"
           nil "ab6883c0a-0342-4007-9966-bc2dfa6b109e")
      (is (= (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e") (parse-uuid "B6883C0A-0342-4007-9966-BC2dfa6b109E")))
      (when-var-exists clojure.core/instance?
        #?(:clj  (is (instance? java.util.UUID (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e"))))
        #?(:cljs (is (instance? cljs.core.UUID (parse-uuid "b6883c0a-0342-4007-9966-bc2dfa6b109e"))))))
    (testing "exceptions"
      #?(:clj (are [x] (thrown? Exception (parse-uuid x))
                   {}
                   '()
                   []
                   #{}
                   \a
                   :key
                   0.0
                   1000))
      #?(:cljr (are [x] (thrown? Exception (parse-uuid x))
                   {}
                   '()
                   []
                   #{}
                   \a
                   :key
                   0.0
                   1000))
      #?(:cljs (are [x] (thrown? js/Error (parse-uuid x))
                   {}
                   '()
                   []
                   #{}
                   :key
                   0.0
                   1000)))))
