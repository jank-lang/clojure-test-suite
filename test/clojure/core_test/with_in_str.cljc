(ns clojure.core-test.with-in-str
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists with-in-str
  (deftest test-with-in-str
    (testing "read from string"
      (is (= "hello" (with-in-str "hello" (read-line)))))

    (testing "multiple lines"
      (is (= "line1" (with-in-str "line1\nline2\nline3" (read-line)))))

    (testing "read multiple lines"
      (is (= ["line1" "line2"] 
             (with-in-str "line1\nline2"
               [(read-line) (read-line)]))))

    (testing "empty string"
      (is (nil? (with-in-str "" (read-line)))))

    (testing "read returns value"
      (is (= 42 (with-in-str "42" (read)))))

    (testing "read multiple values"
      (is (= [1 2 3] (with-in-str "1 2 3" 
                       [(read) (read) (read)]))))

    (testing "restores *in* after"
      (let [original-in *in*]
        (with-in-str "test" (read-line))
        (is (= original-in *in*))))

    (testing "nested with-in-str"
      (is (= "outer" (with-in-str "outer"
                       (with-in-str "inner"
                         (read-line))
                       (read-line)))))

    (testing "with slurp"
      (is (= "hello\nworld" (with-in-str "hello\nworld" 
                              (slurp *in*)))))

    (testing "read with eof"
      (is (= :eof (with-in-str "" (read *in* false :eof)))))

    (testing "returns last expression"
      (is (= :result (with-in-str "test" :result))))))
