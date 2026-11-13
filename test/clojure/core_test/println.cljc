(ns clojure.core-test.println
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/println

  ;; DOCSTRING:
  ;; clojure.core/println
  ;; [& more]
  ;; Same as print followed by (newline)

  ;; `println` terminates its output with the platform's newline, so derive
  ;; that newline instead of hard-coding "\n". The first assertion in the
  ;; test below checks that what we derived really is a newline, so a
  ;; `println` that writes nothing cannot make the remaining tests pass
  ;; vacuously.
  (def ^:private nl (with-out-str (println)))

  (deftest test-println
    (testing "the newline that println writes"
      (is (contains? #{"\n" "\r\n"} nl)))

    (testing "with no arguments, only a newline is written"
      (is (= nl (with-out-str (println)))))

    (testing "a single argument is written, followed by a newline"
      (are [expected arg] (= (str expected nl) (with-out-str (println arg)))
        "a string" "a string"
        "nil"      nil
        ":a-keyword" :a-keyword
        "a-symbol" 'a-symbol
        "42"       42
        ""         ""))

    (testing "multiple arguments are separated by a single space"
      (are [expected args] (= (str expected nl) (with-out-str (apply println args)))
        "a b c"     ["a" "b" "c"]
        "1 2 3"     [1 2 3]
        "a nil b"   ["a" nil "b"]
        ":a b 1"    [:a 'b 1]
        "a  b"      ["a" "" "b"]))

    (testing "strings are written without quotes"
      (is (= (str "abc" nl) (with-out-str (println "abc"))))
      (is (= (str "a b" nl) (with-out-str (println "a b"))))
      (is (= (str "say \"hi\"" nl) (with-out-str (println "say \"hi\"")))))

    (testing "a newline is written after the arguments, never before"
      (is (= (str "xy" nl) (with-out-str (print "x") (println "y"))))
      (is (= (str "x" nl "y" nl) (with-out-str (print "x") (println) (println "y")))))

    (testing "the output is exactly what print produces plus a newline"
      (are [args] (= (str (with-out-str (apply print args)) nl)
                     (with-out-str (apply println args)))
        ["a" "b" "c"]
        [1 2 3]
        [nil :a 'b]))

    (testing "successive calls append their output in order"
      (is (= (str "a" nl "b" nl "c" nl)
             (with-out-str (println "a") (println "b") (println "c"))))
      (is (= (str "ab" nl) (with-out-str (print "a") (println "b")))))

    (testing "strings keep any embedded newlines"
      (is (= (str "a" \newline "b" nl) (with-out-str (println (str "a" \newline "b"))))))

    (testing "unicode text is written unchanged"
      (is (= (str "λ→ü" nl) (with-out-str (println "λ→ü")))))

    (testing "characters are written without quotes or escapes"
      (is (= (str "A b c" nl) (with-out-str (println \A \b \c))))
      (is (= (str \space nl) (with-out-str (println \space)))))

    (testing "values nested in collections are written unreadably too"
      (is (= (str "[a b]" nl) (with-out-str (println ["a" "b"]))))
      (is (= (str "{:a b}" nl) (with-out-str (println {:a "b"})))))

    (testing "`*print-readably*` does not cause strings to be quoted"
      (is (= (str "abc" nl)
             (binding [*print-readably* true] (with-out-str (println "abc"))))))))
