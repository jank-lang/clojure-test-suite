(ns clojure.edn-test.read-string
  #?(:cljs (:require-macros [clojure.edn-test.read-string :refer [are-read-as are-read-as-nil are-thrown]]))
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.edn :as edn]
            [clojure.test :refer [are deftest is testing]]))

;; Many of these tests were largely inspired by the ClojureScript reader test suite:
;; https://github.com/clojure/clojurescript/blob/master/src/test/cljs/cljs/reader_test.cljs

(defmacro are-read-as [& pairs]
  `(are [expected# edn#] (= expected# (edn/read-string edn#)) ~@pairs))

(defmacro are-read-as-nil [& edns]
  `(are [edn#] (nil? (edn/read-string edn#)) ~@edns))

(defmacro are-thrown [& edns]
  `(are [edn#] (~'p/thrown? (edn/read-string edn#)) ~@edns))

(defn new-date [v]
  #?(:cljr    (DateTime/Parse v)
     :lpy     (basilisp.lang.util/inst-from-str v)
     :phel    (php/new DateTimeImmutable v)
     :cljs    (js/Date. v)
     :default (clojure.instant/read-instant-date v)))

(defn epoch-millis [date]
  #?(:cljr    (.ToUnixTimeMilliseconds (DateTimeOffset. date))
     :lpy     (* 1000 (.timestamp date))
     :phel    (php/intval (php/-> date (format "Uv")))
     :cljs    (.valueOf date)
     :default (.getTime date)))

(when-var-exists clojure.edn/read-string
  (deftest test-read-string
    (testing "nil and Booleans"
      (are-read-as-nil "" "nil")
      (are-read-as
        true "true"
        false "false"))

    (testing "Whitespace Only"
      (are-read-as-nil
        " "
        "  "
        ","
        ",,"
        "\t"
        "\r"
        "\n"
        "\r\n"))

    (testing "Strings"
      (are-read-as
        "" "\"\""
        "foo" "\"foo\""
        "escape chars \t \r \n \\ \" \b \f" "\"escape chars \\t \\r \\n \\\\ \\\" \\b \\f\"")

      (testing "Multi-line"
        (are-read-as
          "\n" "\"\\n\""
          "\n\n" "\"\\n\\n\""
          "\na" "\"\\na\""
          "a\n" "\"a\\n\""
          "a\nb" "\"a\\nb\""
          "a\nb" "\"a\nb\""
          "a\nb\nc" "\"a\\nb\\nc\""
          "a\nb\nc" "\"a\\nb\\nc\""
          "\n\\n\n" "\"\\n\\\\n\\n\""
          "\n\"\n" "\"\\n\\\"\\n\""
          "\ra\r" "\"\\ra\\r\""
          "\r\na\r\n" "\"\\r\\na\\r\\n\""
          '(def a "\n") "(def a \"\\n\")"))

      (testing "Unicode Escapes"
        (are-read-as
          "A" "\"\\u0041\""
          "AB" "\"\\u0041\\u0042\""
          "→" "\"\\u2192\""
          "a→b" "\"a\\u2192b\""))

      (testing "Unsupported Escapes"
        (are-thrown "\"\\q\""))

      (testing "Malformed Unicode Escapes"
        (are-thrown
          "\"abc \\ua\""                                    ;; truncated
          "\"abc \\x0z  ...etc\""                           ;; incorrect code
          "\"abc \\u0g00 ..etc\"")))                        ;; incorrect code

    (testing "Characters"
      (are-read-as
        \a "\\a"
        \tab "\\tab"
        \return "\\return"
        \newline "\\newline"
        \space "\\space"
        \u1234 "\\u1234"
        \backspace "\\backspace"
        \formfeed "\\formfeed")

      (testing "Backslash Followed by Whitespace"
        ;; the edn spec forbids a backslash followed by whitespace.
        ;; basilisp enforces it; JVM/CLR/cljs read it as the space char.
        #?(:lpy     (are-thrown "\\ ")
           :default (are-read-as \space "\\ "))
        (are-thrown "\\"))

      ;; non-edn extension
      (testing "Octal Escapes"
        (are-thrown "\\o18" "\\o8" "\\o1000")
        #?(;; basilisp does not support octal char escapes
           :lpy     (are-thrown "\\o0" "\\o101" "\\o377")
           :default (let [octal-char #?(:phel php/chr :default char)]
                      (are-read-as
                        (octal-char 00) "\\o0"
                        (octal-char 0101) "\\o101"
                        (octal-char 0377) "\\o377")))
        ;; above the 377 maximum
        #?(;; phel takes the (mod octal 256) char value
           :phel    (are [v edn] (= (php/chr (mod v 256))
                                    (edn/read-string edn))
                      0400 "\\o400"
                      0477 "\\o477"
                      0777 "\\o777")
           ;; cljs is lenient, reading a broader range of octal characters
           :cljs    (are-read-as
                      (char 0400) "\\o400"
                      (char 0477) "\\o477"
                      (char 0777) "\\o777")
           :default (are-thrown "\\o400" "\\o477" "\\o777"))))

    (testing "Symbols"
      (are-read-as
        'a1b2 "a1b2"
        'foo "foo"
        'foo/bar "foo/bar"
        ;; basilisp fails compilation when using symbol literals on these three
        (symbol "foo.bar" "baz.buzz") "foo.bar/baz.buzz"
        (symbol "foo" "/") "foo//"
        (symbol "foo.bar" "/") "foo.bar//"
        'foo:bar "foo:bar"
        ;; phel fails compilation with 'foo#bar
        (symbol "foo#bar") "foo#bar"
        'nilable "nilable"
        'truer "truer"
        'falsey "falsey"
        '/ "/"
        '% "%"
        '= "="
        '-> "->"
        '- "-"
        '+ "+"
        '. "."
        '... "..."
        '*earmuffs* "*earmuffs*"
        '?qmark "?qmark"
        '!bang "!bang"
        '&amp "&amp"
        '$dollar "$dollar"
        '_discard "_discard"
        '<lt "<lt"
        '>gt ">gt"
        '.method ".method"
        '.-attr ".-attr"))

    (testing "Keywords"
      (are-read-as
        :1 ":1"
        :a1b2 ":a1b2"
        :foo ":foo"
        :foo/bar ":foo/bar"
        ;; basilisp fails compilation with :foo.bar/baz.buzz
        (keyword "foo.bar" "baz.buzz") ":foo.bar/baz.buzz"
        :foo:bar ":foo:bar"
        ;; phel fails compilation with :foo#bar
        (keyword "foo#bar") ":foo#bar"
        :nilable ":nilable"
        :truer ":truer"
        :falsey ":falsey"
        :% ":%"
        := ":="
        :-> ":->"
        :- ":-"
        :+ ":+"
        :. ":."
        :... ":..."
        :*earmuffs* ":*earmuffs*"
        :?qmark ":?qmark"
        :!bang ":!bang"
        :&amp ":&amp"
        :$dollar ":$dollar"
        :_discard ":_discard"
        :<lt ":<lt"
        :>gt ":>gt"
        :.method ":.method"
        :.-attr ":.-attr")

      (testing "Slash-Only"
        ;; the spec calls :/ illegal. Most readers accept it; phel throws
        #?(:phel    (are-thrown ":/")
           :default (are-read-as (keyword "/") ":/"))
        (are-thrown ":/foo")))

    (testing "Invalid Tokens"
      (are-thrown
        "::"
        "::foo"
        "foo/"
        "/foo"
        "foo/bar/"
        "#"
        "##"
        "##Foo"
        "##Infinity"
        "##-Infinity")
      #?(:cljs    (are-read-as
                    nil "#!shebang"
                    nil "#!/bin/bash"
                    nil "#!/usr/bin/env bb"
                    nil "#!shebang\r1"
                    1 "#!shebang\n1"
                    1 "#!/bin/bash\n1"
                    1 "#!/usr/bin/env bb\n1")
         :default (are-thrown
                    "#!shebang"
                    "#!/bin/bash"
                    "#!/usr/bin/env bb"
                    "#!shebang\r1"
                    "#!shebang\n1"
                    "#!/bin/bash\n1"
                    "#!/usr/bin/env bb\n1"))
      #?(;; CLR is lenient, but does not read 3-part symbols/keywords the same way `symbol` and `keyword` do
         :cljr    (letfn [(part-named [named] [(namespace named) (name named)])]
                    (let [result (edn/read-string "foo/bar/baz")]
                      (is (symbol? result))
                      (is (= ["foo/bar" "baz"] (part-named result))))
                    (let [result (edn/read-string ":foo/bar/baz")]
                      (is (keyword? result))
                      (is (= ["foo/bar" "baz"] (part-named result)))))
         :phel    (are-read-as
                    ;; phel is lenient with the symbols
                    (symbol "foo/bar/baz") "foo/bar/baz"
                    ;; ... but skips subsequent slashes in keywords
                    :foo/bar ":foo/bar/baz")
         ;; JVM is lenient in these cases
         :clj     (are-read-as
                    (symbol "foo/bar/baz") "foo/bar/baz"
                    (keyword "foo/bar/baz") ":foo/bar/baz")
         :default (are-thrown
                    "foo/bar/baz"
                    ":foo/bar/baz")))

    (testing "Symbol/Number Boundary"
      (are-thrown "1a" "-1a" "+1a")
      (are-read-as
        '-. "-."
        '+. "+."
        '.5a ".5a"
        '.5 ".5"
        5.0 "5."))

    (testing "Integers"
      (are-read-as
        1 "1"
        1 "+1"
        -1 "-1"
        42 "42"
        42 "+42"
        -42 "-42")

      (testing "Zero and Signs"
        (are [edn] (zero? (edn/read-string edn)) "0" " 0 " "-0" "+0"))

      ;; non-edn extension - basilisp does not support octal/hex/radix literals
      #?(:lpy nil
         :default
         (testing "Octal, Hex, Radix"
           (are [edn] (= 42 (edn/read-string edn)) "052" "0x2a" "2r101010" "8R52" "16r2a" "36r16")
           (are [edn] (= 42 (edn/read-string edn)) "+052" "+0x2a" "+2r101010" "+8r52" "+16R2a" "+36r16")
           (are [edn] (= -42 (edn/read-string edn)) "-052" "-0X2a" "-2r101010" "-8r52" "-16r2a" "-36R16")))

      (testing "Invalid Octal"
        (are-thrown "08" "09")
        (are-read-as
          0 "00"
          1 "01"
          7 "007"))

      (testing "BigInt"
        (are-read-as
          0N "-0N"
          0N "0N"
          -42N "-42N"
          42N "42N"
          0N "+0N"
          42N "+42N"))

      (testing "Overflow"
        (let [result (edn/read-string "9223372036854775808")]
          #?(:cljr (is (= 9223372036854775808N result))     ;; CLR promotes to BigInt
             :lpy  (is (= 9223372036854775808 result))      ;; basilisp integers use arbitrary precision
             :phel (is (float? result))                     ;; phel is lossy
             :cljs (is (= 9223372036854776000 result))      ;; cljs is lossy
             :clj  (is (= 9223372036854775808N result)))))) ;; JVM promotes to BigInt

    (testing "Floats"
      (are-read-as
        1.5 "1.5"
        1.5 "+1.5"
        -1.5 "-1.5")

      (testing "Symbolic Values"
        (is (NaN? (edn/read-string "##NaN")))
        (is (= ##Inf (edn/read-string "##Inf")))
        (is (= ##-Inf (edn/read-string "##-Inf"))))

      (testing "BigDecimal"
        (are-read-as
          0M "-0M"
          0M "0M"
          0M "+0M"
          0M "0.0M"
          -42M "-42M"
          42M "42M"
          42M "+42M"
          0.1M "0.1M"
          0.001M "0.001M"
          -3.14M "-3.14M"
          3.14M "3.14M"
          3.14M "+3.14M"))

      (testing "Exponents"
        (are-read-as
          0.0 "-0e0"
          0.0 "0e0"
          0.0 "0.0e0"
          0.0 "0.00e0"
          0.0 "0e10"
          -1.0 "-1e0"
          1.0 "1e0"
          42.0 "42e0"
          420.0 "42e1"
          4200.0 "42e2"
          -1234.5 "-1.2345e3"
          1234.5 "1.2345e3")

        (testing "Capital E"
          (are-read-as
            4200.0 "42E2"
            1234.5 "1.2345E3"))

        (testing "Negative Exponents"
          (are-read-as
            0.0 "0e-0"
            0.0 "-0e-10"
            -1.0 "-1e-0"
            1.0 "1e-0"
            -0.42 "-42e-2"
            4.2 "42e-1"
            -12.345 "-123.45e-1"))

        (testing "Signed"
          (are-read-as
            1.23456 "+123.456e-2"
            12345.6 "+123.456e2"
            12345.6 "+123.456e+2"
            -1.23456 "-123.456e-2"
            -12345.6 "-123.456e2"
            -12345.6 "-123.456e+2"))

        (testing "Overflows"
          (is (= ##-Inf (edn/read-string "-1e400")))
          (is (= ##Inf (edn/read-string "1e400")))
          (is (zero? (edn/read-string "1e-400")))
          (is (zero? (edn/read-string "-1e-400")))))

      ;; non-edn extension - basilisp and phel do not support ratios
      #?(:lpy  nil
         :phel nil
         :default
         (testing "Ratios"
           (are [expected edn]
             (let [result (edn/read-string edn)]
               #?(:cljs    (= expected result)
                  :default (and (ratio? result)
                                (= expected result))))
             #?(:cljs 0.5 :default 1/2) "1/2"
             #?(:cljs (/ 2 3) :default 2/3) "2/3"
             #?(:cljs (/ 2 3) :default 2/3) "02/03"
             #?(:cljs (/ 12 345) :default 12/345) "12/345"
             #?(:cljs 0.5 :default 1/2) "+1/2"
             #?(:cljs (/ 2 3) :default 2/3) "+2/3"
             #?(:cljs (/ 2 3) :default 2/3) "+02/03"
             #?(:cljs (/ 12 345) :default 12/345) "+12/345"
             #?(:cljs -0.5 :default -1/2) "-1/2"
             #?(:cljs (/ -2 3) :default -2/3) "-2/3"
             #?(:cljs (/ -2 3) :default -2/3) "-02/03"
             #?(:cljs (/ -12 345) :default -12/345) "-12/345")
           (are [expected edn]
             (let [result (edn/read-string edn)]
               #?(:cljs    (= expected result)
                  :default (and (not (ratio? result))
                                (= expected result))))
             0 "0/1"
             0 "0/2"
             0 "00/1"
             0 "-0/1"
             0 "+0/1"
             1 "1/1"
             1 "2/2"
             2 "4/2"
             -1 "-1/1"
             -1 "-2/2"
             -2 "-4/2"
             1 "12/12")
           (are-thrown "1/-1" "1/+1")
           #?(:cljs    (is (NaN? (edn/read-string "0/0")))
              :default (are-thrown "0/0"))
           #?(:cljs
              (are-read-as
                ##Inf "1/0"
                ##-Inf "-1/0"
                ##Inf "+1/0")
              :default
              (are-thrown "1/0" "-1/0" "+1/0")))))

    (testing "Collections"
      (testing "Lists"
        (letfn [(list= [expected other]
                  (and (= expected other)
                       (list? other)))]
          (are [expected edn] (list= expected (edn/read-string edn))
            '() "()"
            '(()) "(())"
            '(1) "(1)"
            '(3 4) "(3 4)"
            '(7 8 9) "(7 8 9)"
            '(#uuid "550e8400-e29b-41d4-a716-446655440000") "(#uuid \"550e8400-e29b-41d4-a716-446655440000\")"
            ;; phel fails compilation with #inst literals
            #?(:phel    (list (new-date "2010-11-12T13:14:15.666-05:00"))
               :default '(#inst "2010-11-12T13:14:15.666-05:00"))
            "(#inst \"2010-11-12T13:14:15.666-05:00\")"))
        (let [result (edn/read-string "(:a b #{c {:d (:e :f :g)}})")
              nested (->> (last result)
                          (remove #{'c})
                          first
                          :d)]
          (is (= '(:a b #{c {:d (:e :f :g)}}) result))
          (is (list? result))
          (is (list? nested))))

      (testing "Vectors"
        (letfn [(vector= [expected other]
                  (and (= expected other)
                       (vector? other)))]
          (are [expected edn] (vector= expected (edn/read-string edn))
            [] "[]"
            [[]] "[[]]"
            [1] "[1]"
            [3 4] "[3 4]"
            [7 8 9] "[7 8 9]"
            [#uuid "550e8400-e29b-41d4-a716-446655440000"] "[#uuid \"550e8400-e29b-41d4-a716-446655440000\"]"
            ;; phel fails compilation with #inst literals
            #?(:phel    [(new-date "2010-11-12T13:14:15.666-05:00")]
               :default [#inst "2010-11-12T13:14:15.666-05:00"])
            "[#inst \"2010-11-12T13:14:15.666-05:00\"]"))
        (let [result (edn/read-string "[:a b #{c {:d [:e :f :g]}}]")
              nested (->> (last result)
                          (remove #{'c})
                          first
                          :d)]
          (is (= [:a 'b #{'c {:d [:e :f :g]}}] result))
          (is (vector? result))
          (is (vector? nested))))

      (testing "Maps"
        (are-thrown
          "{:a}"
          "{:a :b :c}")
        (are-read-as
          {} "{}"
          {:a 1 :b 2 :c 3} "{:a 1 :b 2 :c 3}"
          {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9} "{:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9}"
          {"a" 1} "{\"a\" 1}"
          {{} 1} "{{} 1}"
          {'a 1} "{a 1}"
          {'foo/bar 1} "{foo/bar 1}"
          {:foo/bar 1} "{:foo/bar 1}"
          {1 2} "{1 2}"
          {[3 4] 5} "{[3 4] 5}"
          {#uuid "550e8400-e29b-41d4-a716-446655440000" 1} "{#uuid \"550e8400-e29b-41d4-a716-446655440000\" 1}"
          {1 #uuid "550e8400-e29b-41d4-a716-446655440000"} "{1 #uuid \"550e8400-e29b-41d4-a716-446655440000\"}"
          ;; phel fails compilation with #inst literals
          #?@(:phel    [{(new-date "2010-11-12T13:14:15.666-05:00") 1} "{#inst \"2010-11-12T13:14:15.666-05:00\" 1}"
                        {1 (new-date "2010-11-12T13:14:15.666-05:00")} "{1 #inst \"2010-11-12T13:14:15.666-05:00\"}"]
              :default [{#inst "2010-11-12T13:14:15.666-05:00" 1} "{#inst \"2010-11-12T13:14:15.666-05:00\" 1}"
                        {1 #inst "2010-11-12T13:14:15.666-05:00"} "{1 #inst \"2010-11-12T13:14:15.666-05:00\"}"])
          ;; non-edn extension - basilisp and phel do not support namespaced maps
          #?@(:lpy     []
              :phel    []
              :default [{:foo/bar 1 :foo/baz {:buzz 2}} "#:foo{:bar 1 :baz {:buzz 2}}"])))

      (testing "Sets"
        (are-read-as
          #{} "#{}"
          #{nil} "#{nil}"
          #{#{}} "#{#{}}"
          #{1} "#{1}"
          #{1 2} "#{1 2}"
          #{:a :b :c} "#{:a :b :c}"
          #{:a "b" 'c [] #{}} "#{:a \"b\" c [] #{}}"
          #{#uuid "550e8400-e29b-41d4-a716-446655440000"} "#{#uuid \"550e8400-e29b-41d4-a716-446655440000\"}"
          ;; phel fails compilation with #inst literals
          #?(:phel    #{(new-date "2010-11-12T13:14:15.666-05:00")}
             :default #{#inst "2010-11-12T13:14:15.666-05:00"})
          "#{#inst \"2010-11-12T13:14:15.666-05:00\"}"))

      (testing "Key Uniqueness"
        (testing "Sets"
          (are-thrown
            "#{a a}"
            "#{:a :a}"
            "#{:foo/bar :foo/bar}"
            "#{1 1}"
            "#{1 1N}"
            "#{[] ()}"
            "#{#{} #{}}"
            "#{{} {}}"
            "#{#inst \"2010-11-12T13:14:15.666-05:00\" #inst \"2010-11-12T13:14:15.666-05:00\"}"
            "#{#uuid \"550e8400-e29b-41d4-a716-446655440000\" #uuid \"550e8400-e29b-41d4-a716-446655440000\"}"))

        (testing "Maps"
          (are-thrown
            "{a 1 a 2}"
            "{:a 1 :a 2}"
            "{:foo/bar 1 :foo/bar 2}"
            "{foo/bar 1 foo/bar 2}"
            "{1 :a 1 :b}"
            "{1 :a 1N :b}"
            "{[] 1 () 2}"
            "{#{} 1 #{} 2}"
            "{{} 1 {} 2}"
            "{#inst \"2010-11-12T13:14:15.666-05:00\" 1 #inst \"2010-11-12T13:14:15.666-05:00\" 2}"
            "{#uuid \"550e8400-e29b-41d4-a716-446655440000\" 1 #uuid \"550e8400-e29b-41d4-a716-446655440000\" 2}"))

        (testing "BigInt / BigDecimal"
          (are [edn] #?(:cljs    (p/thrown? (edn/read-string edn))
                        :default (= 2 (count (edn/read-string edn))))
            "#{1 1.0}"
            "#{1M 1}"
            "#{1M 1N}"
            "#{1M 1.0}"
            "#{1.0M 1.0}"
            "{1 2 1.0 3}"
            "{1M 2 1 3}"
            "{1M 2 1N 3}"
            "{1M 2 1.0 3}"
            "{1.0M 2 1.0 3}"))))

    (testing "Tagged Elements"
      (testing "Instants"
        (are-thrown
          "#inst 0"
          "#inst \"\""
          "#inst \"not-an-inst\"")
        (let [est-edn       "#inst \"2010-11-12T13:14:15.666-05:00\""
              est-inst      (edn/read-string est-edn)
              est-inst-copy (edn/read-string est-edn)
              utc-inst      (edn/read-string "#inst \"2010-11-12T18:14:15.666-00:00\"")
              est-date      (new-date "2010-11-12T13:14:15.666-05:00")]
          (are [inst-1 inst-2] (= (epoch-millis inst-1) (epoch-millis inst-2))
            est-date est-inst
            est-inst est-inst-copy
            est-inst utc-inst))
        (are [millis edn] (= millis (-> edn edn/read-string epoch-millis))
          1262330055666 "#inst \"2010-01-01T01:14:15.666-06:00\"" ;; all single-digit (zero-padded)
          1284045255666 "#inst \"2010-09-09T09:14:15.666-06:00\"" ;; single/double-digit boundary
          1286727255666 "#inst \"2010-10-10T10:14:15.666-06:00\"" ;; first double-digit values
          1293596055666 "#inst \"2010-12-28T22:14:15.666-06:00\"")) ;; range extremes

      (testing "Instant RFC3339 Formats"
        (let [zulu (-> "#inst \"2010-11-12T18:14:15.666Z\"" edn/read-string epoch-millis)]
          (are [edn] (= zulu (-> edn edn/read-string epoch-millis))
            "#inst \"2010-11-12T18:14:15.666-00:00\""
            "#inst \"2010-11-12T18:14:15.666+00:00\""
            "#inst \"2010-11-12T13:14:15.666-05:00\""
            "#inst \"2010-11-12T23:14:15.666+05:00\""))
        (are [millis edn] (= millis (-> edn edn/read-string epoch-millis))
          1289567655000 "#inst \"2010-11-12T13:14:15Z\""    ;; no fractional seconds
          482196050520 "#inst \"1985-04-12T23:20:50.52Z\""  ;; 2-digit fractional seconds
          1289538855666 "#inst \"2010-11-12T13:14:15.666+08:00\"")) ;; positive UTC offset

      (testing "Date Only Instant"
        (let [edn    "#inst \"2026-02-03\""
              millis 1770076800000]
          #?(;; basilisp reads date-only instants with local timezone
             :lpy     (is (= (new-date "2026-02-03") (edn/read-string edn)))
             :phel    (are-thrown edn)
             :default (is (= millis (epoch-millis (edn/read-string edn)))))))

      (testing "UUIDs"
        ;; cljs seems to allow malformed uuids
        #?(:cljs    (is (= (uuid "not-a-uuid") (edn/read-string "#uuid \"not-a-uuid\"")))
           :default (are-thrown "#uuid \"not-a-uuid\""))
        (are-thrown "#uuid 0")
        (let [uid-edn "#uuid \"550e8400-e29b-41d4-a716-446655440000\""
              uid     #uuid "550e8400-e29b-41d4-a716-446655440000"]
          (are-read-as uid uid-edn)
          ;; CLR uuids are value types; always `identical?`
          #?(:cljr    (is (identical? uid (edn/read-string uid-edn)))
             :default (is (not (identical? uid (edn/read-string uid-edn)))))))

      (testing "Unknown Tag"
        (are-thrown "#unknown 0"))

      (testing "Variable Whitespace"
        (let [uid #uuid "550e8400-e29b-41d4-a716-446655440000"]
          (are [edn] (= uid (edn/read-string edn))
            "#uuid\"550e8400-e29b-41d4-a716-446655440000\"" ;; no space
            "#uuid  \"550e8400-e29b-41d4-a716-446655440000\"" ;; double space
            "#uuid\n\"550e8400-e29b-41d4-a716-446655440000\"" ;; newline
            "#uuid\t\"550e8400-e29b-41d4-a716-446655440000\"" ;; tab
            "#uuid\r\"550e8400-e29b-41d4-a716-446655440000\"" ;; return
            "#uuid,\"550e8400-e29b-41d4-a716-446655440000\"" ;; comma
            "#uuid #_foo\"550e8400-e29b-41d4-a716-446655440000\""))) ;; discard

      (testing "Tag Without Element"
        (are-thrown
          "#foo"
          "#my/bar"
          "#inst"
          "#uuid"))

      (testing "Whitespace After Dispatch"
        ;; the dispatch char must immediately follow # - phel returns nil instead
        (are [edn] #?(:phel    (nil? (edn/read-string edn))
                      :default (p/thrown? (edn/read-string edn)))
          "# foo"
          "#  foo"
          "#,foo"
          "#\tfoo"
          "#\rfoo"
          "#\nfoo"
          "# {}"
          "#  {}"
          "#,{}"
          "#\t{}"
          "#\r{}"
          "#\n{}"
          "# _"
          "#  _"
          "#,_"
          "#\t_"
          "#\r_"
          "#\n_")))

    (testing "Reading with Options"

      (testing "EOF"
        (are [expected edn] (= expected (edn/read-string {:eof :END} edn))
          ;; cljs short-circuits to nil on empty strings
          #?(:cljs nil :default :END) ""
          :END " "
          :END ";just a comment\n"
          42 "42"))

      (testing "Default Reader"
        (are [expected edn] (= expected (edn/read-string {:default (fn [_tag v] [:unknown v])} edn))
          [:unknown 42] "#foo 42"
          [:unknown 42] "#foo/bar 42"
          #uuid "550e8400-e29b-41d4-a716-446655440000" "#uuid \"550e8400-e29b-41d4-a716-446655440000\""))

      (testing "Custom Readers"
        (are [expected readers edn] (= expected (edn/read-string {:readers readers} edn))
          [:foo 42] {'my/foo (fn [x] [:foo x])} "#my/foo 42"
          :override {'uuid (constantly :override)} "#uuid \"550e8400-e29b-41d4-a716-446655440000\""
          :override {'inst (constantly :override)} "#inst \"2010-11-12T13:14:15.666-05:00\""
          [:a [:b 42]] {'my/a (fn [x] [:a x]) 'my/b (fn [x] [:b x])} "#my/a #my/b 42"
          {:point [1 2]} {'my/point (fn [v] {:point v})} "#my/point [1 2]"))

      (testing "Readers with Default"
        (let [opts {:readers {'my/foo (fn [x] [:foo x])}
                    :default (fn [tag v] [:default tag v])}]
          (are [expected edn] (= expected (edn/read-string opts edn))
            [:foo 42] "#my/foo 42"
            [:default 'my/bar 42] "#my/bar 42")))

      (testing "Unsupplied EOF"
        ;; basilisp and phel read these as nil - all others throw
        (are [edn] #?(:lpy     (nil? (edn/read-string {} edn))
                      :phel    (nil? (edn/read-string {} edn))
                      :default (p/thrown? (edn/read-string {} edn)))
          " "
          ";just a comment\n")))

    (testing "Comments and Discard"
      (are-read-as
        nil ";foo"
        nil ";foo\n"
        3 ";foo\n3"
        3 ";foo\n3\n5"
        ;; phel and cljs do not consider returns as newlines for comments
        #?(:phel 5 :cljs 5 :default 3) ";foo\r3\n5"
        nil ";#inst \"2010-11-12T13:14:15.666-05:00\""
        nil "#_nope"
        3 "#_nope 3"
        [2 3] "[;foo1\n2 3]"
        [1 3] "[1;foo2\n3]"
        [2 3] "[#_1 2 3]"
        [1 3] "[1 #_2 3]"
        [1 2] "[1 2 #_3]"
        [1 4] "[1 #_2 #_3 4]"
        [1 4] "[1 #_ #_ 2 3 4]"
        [1 3] "[1 #_,\t\n\r 2 3]"
        [4] "[#_(1 2 3) 4]"
        [4] "[#_[1 2 3] 4]"
        [2] "[#_{:a 1} 2]"
        [3] "[#_#{1 2} 3]"
        '(2 3) "(;foo1\n2 3)"
        '(1 3) "(1;foo2\n3)"
        '(2 3) "(#_1 2 3)"
        '(1 3) "(1 #_2 3)"
        '(1 2) "(1 2 #_3)"
        '(1 4) "(1 #_2 #_3 4)"
        '(1 4) "(1 #_ #_ 2 3 4)"
        '(1 3) "(1 #_,\t\n\r 2 3)"
        '(4) "(#_(1 2 3) 4)"
        '(4) "(#_[1 2 3] 4)"
        '(2) "(#_{:a 1} 2)"
        '(3) "(#_#{1 2} 3)"
        {:a 1} "{:a;foo:b\n1}"
        {:a 1 :c 3} "{:a 1;:b 2\n:c 3}"
        {:a 1 :c 3} "{:a 1 #_:b #_2 :c 3}"
        {:a 1} "{:a #_nope 1}"
        {:a 2} "{:a #_,\t\n\r 1 2}"
        {:a 1 :c 3} "{:a 1 #_:b #_2 :c 3}"
        #{1 3} "#{1;foo2\n3}"
        #{2 3} "#{#_1 2 3}"
        #{1 3} "#{1 #_2 3}"
        #{1 2} "#{1 2 #_3}"
        #{1 3} "#{1 #_,\t\n\r 2 3}")

      (testing "Discarding Metadata"
        (are-read-as-nil "#_ ^:foo {}")
        (are-thrown
          "#_ ^:foo"
          "#_ ^String"
          "#_ ^String \"\""))

      (testing "Discarding Tagged Elements"
        ;; phel is the only dialect that ignores discarded tags' readers
        #?(:phel
           (are-read-as-nil
             "#_ #inst \"not-an-instant\""
             "#_ #foo 0"
             "#_ #foo/bar 0")
           :default
           (are-thrown
             "#_ #inst \"not-an-instant\""
             "#_ #foo 0"
             "#_ #foo/bar 0"))
        ;; cljs cannot discard tagged elements
        #?(:cljs
           (are-thrown
             "#_ #inst \"2010-11-12T13:14:15.666-05:00\""
             "(#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1)"
             "[#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1]"
             "{:a #_ #inst \"2010-11-12T13:14:15.666-05:00\" 1}"
             "#{#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1}")
           :default
           (are-read-as
             nil "#_ #inst \"2010-11-12T13:14:15.666-05:00\""
             '(1) "(#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1)"
             [1] "[#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1]"
             {:a 1} "{:a #_ #inst \"2010-11-12T13:14:15.666-05:00\" 1}"
             #{1} "#{#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1}")))

      (testing "Discard Without Element"
        (are-thrown
          "#_"
          "[#_]"
          "(#_)"
          "{#_}"
          "#{#_}"
          "#_ #_ 1"))

      (testing "Discard Skips Tag Handlers"
        ;; the spec says a reader should NOT invoke tag handlers while reading a
        ;; discarded element. Only phel honors this.
        (let [opts {:readers {'my/boom (fn [_] (throw (ex-info "handler called" {})))}}]
          #?(:phel    (is (= [42] (edn/read-string opts "[#_ #my/boom 0 42]")))
             :default (is (p/thrown? (edn/read-string opts "[#_ #my/boom 0 42]")))))))

    (testing "Whitespace Between Forms"
      (are-read-as
        [1 2] "[1 2]"
        [1 2] "[1  2]"
        [1 2] "[1,2]"
        [1 2] "[1\t2]"
        [1 2] "[1\n2]"
        [1 2] "[1\r2]")

      (testing "Multiple Forms"
        (are-read-as
          'first "first second"
          1 "1 2 3"
          :a ":a :b :c"
          "one" "\"one\" \"two\""
          nil "nil\n1"
          1 "\n1"))

      (testing "Commas"
        (are-read-as
          nil ",nil,"
          true ",true,"
          false ",false,"
          0 ",0,"
          -1 ",-1,"
          1 ",1,"
          -1.5 ",-1.5,"
          "foo" ",\"foo\","
          ",,," ",\",,,\","
          "Hello, World!" ",\"Hello, World!\","
          :hello ",:hello,"
          'goodbye ",goodbye,"
          :foo/bar ",:foo/bar,"
          'foo/bar ",foo/bar,"
          #{1 2 3} ",#{1, 2, 3},"
          '(1 2 3) ",(1, 2, 3),"
          '[1 2 3] ",[1, 2, 3],"
          {:a 1 :b 2} ",{:a 1, :b 2},"
          \a ",\\a,")))

    (testing "Unicode"
      (are-read-as
        "اختبار" "\"اختبار\""                               ;; arabic
        "ทดสอบ" "\"ทดสอบ\""                                 ;; thai
        "こんにちは" "\"こんにちは\""                                 ;; japanese hiragana
        "你好" "\"你好\""                                       ;; chinese traditional
        "אַ גוט יאָר" "\"אַ גוט יאָר\""                     ;; yiddish
        "cześć" "\"cześć\""                                 ;; polish
        "привет" "\"привет\""                               ;; russian

        'اختبار "اختبار"
        'ทดสอบ "ทดสอบ"
        'こんにちは "こんにちは"
        '你好 "你好"
        ;; basilisp fails compilation with a literal 'אַ symbol
        (symbol "אַ") "אַ גוט יאָר"
        'cześć "cześć"
        'привет "привет"

        :اختبار ":اختبار"
        :ทดสอบ ":ทดสอบ"
        :こんにちは ":こんにちは"
        :你好 ":你好"
        ;; basilisp fails compilation with a literal :אַ keyword
        (keyword "אַ") ":אַ גוט יאָר"
        :cześć ":cześć"
        :привет ":привет"

        {:привет :ru "你好" :cn} "{:привет :ru \"你好\" :cn}"))

    (testing "Metadata"
      (let [edn "^String {:a 1}"]
        ;; non-edn extension - basilisp does not support ^ metadata
        #?(:lpy     (are-thrown edn)
           :default (is (= {:tag 'String} (meta (edn/read-string edn)))))))

    (testing "Malformed Input"
      (testing "Non-String Inputs"
        ;; phel stringifies non-string input rather than throwing
        #?(:phel    (is (= :foo (edn/read-string :foo)))
           :default (are-thrown :foo)))

      (testing "Unbalanced Delimiters"
        (are-thrown
          "["
          "("
          "{"
          "#{"
          "]"
          ")"
          "}"
          "\""
          "[[]"
          "(()"
          "{{}"
          "#{{}"
          "[1 2"
          "(1 2"
          "{1 2"
          "#{1 2"
          "\"foo")))))
