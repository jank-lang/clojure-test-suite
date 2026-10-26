(ns clojure.string-test.split
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists str/split
  (deftest test-split
    (testing "basic input"
      (is (= [] (str/split "a" #"a")))
      (is (= ["" " "] (str/split "a " #"a")))
      (is (= [" "] (str/split " a" #"a")))
      (is (= [" " " "] (str/split " a " #"a")))
      (is (= ["a"] (str/split "a" #"b"))))

    (testing "positive limit"
      (is (= [""] (str/split "" #"" 1)))
      (is (= ["a"] (str/split "a" #"a" 1)))
      (is (= ["" ""] (str/split "a" #"a" 2)))
      (is (= ["aaaaaaaaaa"] (str/split "aaaaaaaaaa" #"" 1))))

    (testing "negative limit"
      (is (= ["a" "" ""]
             (str/split "a\n\n" #"\n" -1)))
      (is (= ["a" "" ""]
             (str/split "a\n\n" #"\n" -10)))
      (is (= ["a" "" ""]
             (str/split "a\n\n" #"\n" -100))))

    #?(:cljs
       (testing "dialect quirks - cljs"
         (testing "basic input"
           (is (= [] (str/split "" #"")))
           (is (= ["" "a"] (str/split "a" #""))))
         (testing "positive limit"
           (is (= ["" ""] (str/split "" #"" 10)))
           (is (= ["" "a" "a" "a" "a" "a" "a" "a" "a" "aa"]
                  (str/split "aaaaaaaaaa" #"" 10)))))

       :cljr
       (testing "dialect quirks - cljr"
         (testing "basic input"
           (is (= [] (str/split "" #"")))
           (is (= ["" "a"] (str/split "a" #""))))
         (testing "positive limit"
           (is (= ["" ""] (str/split "" #"" 10)))
           (is (= ["" "a" "a" "a" "a" "a" "a" "a" "a" "aa"]
                  (str/split "aaaaaaaaaa" #"" 10)))))

       :default
       (testing "dialect quirks - default"
         (testing "basic input"
           (is (= [""] (str/split "" #"")))
           (is (= ["a"] (str/split "a" #""))))
         (testing "positive limit"
           (is (= [""] (str/split "" #"" 10)))
           (is (= ["a" "a" "a" "a" "a" "a" "a" "a" "a" "a"]
                  (str/split "aaaaaaaaaa" #"" 10))))))

    (testing "Examples from https://clojuredocs.org/clojure.string/split"
      (is (= ["Clojure" "is" "awesome!"]
             (str/split "Clojure is awesome!" #" ")))

      (is (= ["q" "w" "e" "r" "t" "y" "u" "i" "o" "p"]
             (str/split "q1w2e3r4t5y6u7i8o9p0" #"\d+")))
      (is (= ["q" "w" "e" "r" "t5y6u7i8o9p0"]
             (str/split "q1w2e3r4t5y6u7i8o9p0" #"\d+" 5)))
      (is (= #?(:cljs    ["" " " "q" "1" "w" "2" " "]
                :cljr    ["" " " "q" "1" "w" "2" " "]
                :default [" " "q" "1" "w" "2" " "])
             (str/split " q1w2 " #"")))

      (is (= [" something and " "A" "Camel" "Name "]
             (str/split " something and ACamelName " #"(?=[A-Z])")))

      (is (= ["a"] (str/split "a" #"b")))
      (is (= [" "] (str/split " " #"b")))
      (is (= #?(:cljr [] :default [""]) (str/split "" #"b")))
      (is (= [] (str/split "a" #"a")))
      (is (= [] (str/split "aaa" #"a")))
      (is (= #?(:cljs [] :cljr [] :default [""]) (str/split "" #"")))

      (is (= ["Some" "words" "to" "split"]
             (str/split "Some words to split" #"\s+")))

      (is (= ["Some" "words" "with" "other" "whitespace"]
             (str/split "Some    words   with\tother whitespace      \n" #"\s+")))

      (is (= ["" "Leading" "whitespace" "causes" "empty" "first" "string"]
             (str/split "   Leading whitespace causes empty first string" #"\s+")))

      (is (= ["root" "*" "0" "0" "admin" "/var/root" "/bin/sh"]
             (str/split "root:*:0:0:admin:/var/root:/bin/sh" #":")))
      (is (= ["root" "" "0" "0" "" "/var/root" "/bin/sh"]
             (str/split "root::0:0::/var/root:/bin/sh" #":")))
      (is (= ["root" "" "0" "0" "admin" "/var/root"]
             (str/split "root::0:0:admin:/var/root:" #":")))
      (is (= ["root" "" "0" "0" "admin"]
             (str/split "root::0:0:admin::" #":")))
      (is (= ["root" "" "0" "0" "admin" "/var/root" ""]
             (str/split "root::0:0:admin:/var/root:" #":" -1)))
      (is (= ["root" "" "0" "0" "admin" "" ""]
             (str/split "root::0:0:admin::" #":" -1)))

      (is (= ["root" ":0:0:admin:/var/root:"]
             (str/split "root::0:0:admin:/var/root:" #":" 2)))
      (is (= ["root" "" "0:0:admin:/var/root:"]
             (str/split "root::0:0:admin:/var/root:" #":" 3)))
      (is (= ["root" "" "0" "0:admin:/var/root:"]
             (str/split "root::0:0:admin:/var/root:" #":" 4)))
      (is (= ["root" "" "0" "0" "admin" "/var/root" ""]
             (str/split "root::0:0:admin:/var/root:" #":" 15))))))