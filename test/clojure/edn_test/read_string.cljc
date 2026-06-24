(ns clojure.edn-test.read-string
  #?(:cljs (:require-macros [clojure.edn-test.read-string :refer [are-read-as are-thrown]]))
  (:require [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]
            [clojure.edn :as edn]
            [clojure.test :refer [are deftest is testing]]))

;; Many of these tests were largely inspired by the ClojureScript reader test suite:
;; https://github.com/clojure/clojurescript/blob/master/src/test/cljs/cljs/reader_test.cljs

(defmacro are-read-as [& pairs]
  `(are [expected# edn#] (= expected# (edn/read-string edn#)) ~@pairs))

(defmacro are-thrown [& edns]
  `(are [edn#] (~'p/thrown? (edn/read-string edn#)) ~@edns))

(defn epoch-millis [date]
  #?(:cljr    (.ToUnixTimeMilliseconds (DateTimeOffset. date))
     :lpy     (* 1000 (.timestamp date))
     :phel    (php/intval (php/-> date (format "Uv")))
     :cljs    (.valueOf date)
     :default (.getTime date)))

(when-var-exists clojure.edn/read-string
  (deftest test-read-string
    (testing "Throws Eval Reader"
      (are-thrown (edn/read-string "#=(+ 1 2)")))

    (testing "nil and Booleans"
      (are-read-as
        nil "nil"
        true "true"
        false "false"))

    (testing "Whitespace Only"
      (are-read-as
        nil ""
        nil "  , ,, \t \r \n \r\n"))

    (testing "Strings"
      (are-read-as
        "" "\"\""
        "foo" "\"foo\""
        "escape chars \t \r \n \\ \" \b \f" "\"escape chars \\t \\r \\n \\\\ \\\" \\b \\f\"")

      (testing "Multi-line"
        (are-read-as
          "\n" "\"\n\""
          "\n\n" "\"\\n\\n\""
          "\r\n" "\"\r\n\""
          "a\nb\nc" "\"a\\nb\nc\""
          '(def a "\n") "(def a \"\\n\")"))

      (testing "Unicode Escapes"
        (are-read-as
          "A" "\"\\u0041\""
          "AB" "\"\\u0041\\u0042\""
          "a→b" "\"a\\u2192b\""))

      (testing "Malformed Escapes"
        (are-thrown
          "\"\\q\""                                         ;; unsupported
          "\"abc \\u000\""                                  ;; truncated
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
        (are-read-as \space "\\ ")
        (are-thrown "\\"))

      (testing "Octal Escapes"
        (are-thrown "\\o18" "\\o8" "\\o1000")
        (are-read-as
          (char 00) "\\o0"
          (char 0101) "\\o101"
          (char 0377) "\\o377")
        ;; above 0377
        #?(;; cljs is lenient, reading a broader range of octal characters
           :cljs    (are-read-as
                      (char 0400) "\\o400"
                      (char 0477) "\\o477"
                      (char 0777) "\\o777")
           :default (are-thrown "\\o400" "\\o477" "\\o777"))))

    (testing "Symbols"
      (are-read-as
        'a1b2 "a1b2"
        'foo/bar "foo/bar"
        'foo.bar/baz.buzz "foo.bar/baz.buzz"
        'foo// "foo//"
        'foo:bar "foo:bar"
        'foo#bar "foo#bar"
        'nilable "nilable"
        'truer "truer"
        'falsey "falsey"
        '/ "/"
        '%percent "%percent"
        '=eq "=eq"
        '-minus "-minus"
        '+plus "+plus"
        '.dot ".dot"
        '*star "*star"
        '?qmark "?qmark"
        '!bang "!bang"
        '&amp "&amp"
        '$dollar "$dollar"
        '_discard "_discard"
        '<lt "<lt"
        '>gt ">gt"))

    (testing "Keywords"
      (are-read-as
        :1 ":1"
        :a1b2 ":a1b2"
        :foo/bar ":foo/bar"
        :foo.bar/baz.buzz ":foo.bar/baz.buzz"
        :foo:bar ":foo:bar"
        :foo#bar ":foo#bar"
        :/ ":/"
        :%percent ":%percent"
        :=eq ":=eq"
        :-minus ":-minus"
        :+plus ":+plus"
        :.dot ":.dot"
        :*star ":*star"
        :?qmark ":?qmark"
        :!bang ":!bang"
        :&amp ":&amp"
        :$dollar ":$dollar"
        :_discard ":_discard"
        :<lt ":<lt"
        :>gt ":>gt"))

    (testing "Invalid Tokens"
      (are-thrown
        ":"
        "::"
        "::foo"
        ":/foo"
        "foo/"
        "/foo"
        "#"
        "##"
        "##Infinity"
        "##-Infinity")
      #?(:cljs    (are-read-as
                    nil "#!shebang"
                    1 "#!shebang\r\n1")
         :default (are-thrown
                    "#!shebang"
                    "#!shebang\r\n1"))
      #?(;; CLR is lenient, but does not read 3-part symbols/keywords the same way `symbol` and `keyword` do
         :cljr    (letfn [(part-named [named] [(namespace named) (name named)])]
                    (let [result (edn/read-string "foo/bar/baz")]
                      (is (symbol? result))
                      (is (= ["foo/bar" "baz"] (part-named result))))
                    (let [result (edn/read-string ":foo/bar/baz")]
                      (is (keyword? result))
                      (is (= ["foo/bar" "baz"] (part-named result)))))
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
        '.5 ".5"
        5.0 "5."))

    (testing "Integers"
      (are-read-as
        42 "42"
        42 "+42"
        -42 "-42")

      (testing "Zero and Signs"
        (are [edn] (zero? (edn/read-string edn)) "0" " 0 " "-0" "+0"))

      (testing "Octal, Hex, Radix"
        (are-thrown "08" "0x2g" "2r2" "8R8")
        (are [edn] (= 42 (edn/read-string edn)) "052" "0x2a" "2r101010" "8R52" "16r2a" "36r16")
        (are [edn] (= 42 (edn/read-string edn)) "+052" "+0x2a" "+2r101010" "+8r52" "+16R2a" "+36r16")
        (are [edn] (= -42 (edn/read-string edn)) "-052" "-0X2a" "-2r101010" "-8r52" "-16r2a" "-36R16"))

      (testing "BigInt"
        (are-read-as
          0N "0N"
          -42N "-42N"
          42N "42N"
          42N "+42N"))

      (testing "Overflow"
        (let [result (edn/read-string "9223372036854775808")]
          #?(:cljr (is (= 9223372036854775808N result))     ;; CLR promotes to BigInt
             :lpy  (is (= 9223372036854775808 result))      ;; basilisp integers use arbitrary precision
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
          0M "0.0M"
          42M "42M"
          -3.14M "-3.14M"
          3.14M "3.14M"
          3.14M "+3.14M"))

      (testing "Exponents"
        (are-read-as
          0.0 "0e0"
          42.0 "42e0"
          -4.2 "-42e-1"
          420.0 "42E1"
          12.345 "1234.5e-2"
          -123.45 "-1.2345e2"
          123.45 "+1.2345e+2"
          123.45 "1.2345E2"
          ##-Inf "-1e400"
          ##Inf "1e400"
          0.0 "1e-400"
          0.0 "-1e-400"))

      (testing "Ratios"
        (are [expected edn]
          (let [result (edn/read-string edn)]
            #?(:cljs    (= expected result)
               :default (and (ratio? result)
                             (= expected result))))
          #?(:cljs 0.5 :default 1/2) "1/2"
          #?(:cljs -0.5 :default -1/2) "-1/2"
          #?(:cljs (/ 2 3) :default 2/3) "+02/03"
          #?(:cljs (/ -2 3) :default -2/3) "-02/03"
          #?(:cljs (/ 12 345) :default 12/345) "12/345")
        (are [expected edn]
          (let [result (edn/read-string edn)]
            #?(:cljs    (= expected result)
               :default (and (not (ratio? result))
                             (= expected result))))
          0 "0/1"
          1 "1/1"
          2 "4/2"
          -1 "-12/12")
        (are-thrown "1/-1" "1/+1")
        #?(:cljs    (is (NaN? (edn/read-string "0/0")))
           :default (are-thrown "0/0"))
        #?(:cljs
           (are-read-as
             ##Inf "1/0"
             ##-Inf "-1/0"
             ##Inf "+1/0")
           :default
           (are-thrown "1/0" "-1/0" "+1/0"))))

    (testing "Collections"
      (testing "Lists"
        (are-read-as
          '() "()"
          '(()) "(())")
        (let [result (edn/read-string "(:a b {:c (:d :e :f)})")
              nested (:c (last result))]
          (is (= '(:a b {:c (:d :e :f)}) result))
          (is (list? result))
          (is (list? nested))))

      (testing "Vectors"
        (are-read-as
          [] "[]"
          [[]] "[[]]")
        (let [result (edn/read-string "[:a b {:c [:d :e :f]}]")
              nested (:c (last result))]
          (is (= [:a 'b {:c [:d :e :f]}] result))
          (is (vector? result))
          (is (vector? nested))))

      (testing "Maps"
        (are-thrown "{:a}" "{:a :b :c}")
        (are-read-as
          {} "{}"
          {:a 1 'b #{} '() [] nil "c"} "{:a 1 b #{} () [] nil \"c\"}"
          {:foo/bar 1 :foo/baz {:buzz 2}} "#:foo{:bar 1 :baz {:buzz 2}}"
          {:a #uuid "550e8400-e29b-41d4-a716-446655440000"} "{:a #uuid \"550e8400-e29b-41d4-a716-446655440000\"}"))

      (testing "Sets"
        (are-read-as
          #{} "#{}"
          #{:a "b" 'c 1 [] #{} nil} "#{:a \"b\" c 1 [] #{} nil}"
          #{#uuid "550e8400-e29b-41d4-a716-446655440000"} "#{#uuid \"550e8400-e29b-41d4-a716-446655440000\"}"))

      (testing "Key Uniqueness"
        (are-thrown
          "#{:a :a}"
          "#{#uuid \"550e8400-e29b-41d4-a716-446655440000\" #uuid \"550e8400-e29b-41d4-a716-446655440000\"}"
          "{:a 1 :a 2}"
          "{#uuid \"550e8400-e29b-41d4-a716-446655440000\" 1 #uuid \"550e8400-e29b-41d4-a716-446655440000\" 2}")))

    (testing "Tagged Elements"
      (testing "Instants"
        (are-thrown
          "#inst 0"
          "#inst \"\""
          "#inst \"not-an-inst\""
          "#inst \"2010-02-29T00:00:00.000Z\""
          "#inst \"2010-01-01T24:00:00.000Z\"")
        (are [millis edn] (= millis (-> edn edn/read-string epoch-millis))
          1262311321001 "#inst \"2010-01-01T01:01:01.001-01:01\"" ;; all single-digit (zero-padded)
          1284056289009 "#inst \"2010-09-09T09:09:09.009-09:09\"" ;; single/double-digit boundary
          1286742010010 "#inst \"2010-10-10T10:10:10.010-10:10\"" ;; first double-digit values
          ;; range extremes - cljr throws offsets greater than 14 hours
          #?@(:cljr    [1293890399999 "#inst \"2010-12-31T23:59:59.999-14:00\""]
              :default [1293926339999 "#inst \"2010-12-31T23:59:59.999-23:59\""]))

        (testing "Instant Formats"
          (are [edn] (= 1289585655666 (-> edn edn/read-string epoch-millis))
            "#inst \"2010-11-12T18:14:15.666Z\""
            "#inst \"2010-11-12T18:14:15.666-00:00\""
            "#inst \"2010-11-12T18:14:15.666+00:00\""
            "#inst \"2010-11-12T13:14:15.666-05:00\""
            "#inst \"2010-11-12T23:14:15.666+05:00\"")
          (are [millis edn] (= millis (-> edn edn/read-string epoch-millis))
            1289567655000 "#inst \"2010-11-12T13:14:15Z\""  ;; no fractional seconds
            482196050520 "#inst \"1985-04-12T23:20:50.52Z\"" ;; 2-digit fractional seconds
            1770076800000 "#inst \"2026-02-03\"")))         ;; date only

      (testing "UUIDs"
        ;; cljs seems to allow malformed uuids
        #?(:cljs    (is (= (uuid "not-a-uuid") (edn/read-string "#uuid \"not-a-uuid\"")))
           :default (are-thrown "#uuid \"not-a-uuid\""))
        (are-thrown "#uuid 0")
        (let [parsed-uid (edn/read-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")
              uid        #uuid "550e8400-e29b-41d4-a716-446655440000"]
          (is (= uid parsed-uid))
          ;; CLR uuids are value types; always `identical?`
          #?(:cljr    (is (identical? uid parsed-uid))
             :default (is (not (identical? uid parsed-uid))))))

      (testing "Unknown Tag"
        (are-thrown "#unknown 0"))

      (testing "Variable Whitespace"
        (let [uid #uuid "550e8400-e29b-41d4-a716-446655440000"]
          (are-read-as
            uid "#uuid\"550e8400-e29b-41d4-a716-446655440000\"" ;; no space
            uid "#uuid  ;comment\r\n\t,#_foo\"550e8400-e29b-41d4-a716-446655440000\""))) ;; whitespace / comments

      (testing "Tag Without Element"
        (are-thrown "#foo" "#my/bar" "#inst" "#uuid"))

      (testing "Whitespace After Dispatch"
        ;; cljs is lenient here
        (are [edn] #?(:cljs    (edn/read-string edn)
                      :default (p/thrown? (edn/read-string edn)))
          "# ,\t\r\ninst \"2010-11-12T18:14:15.666Z\""
          "# ,\t\r\nuuid \"550e8400-e29b-41d4-a716-446655440000\"")
        (are-thrown
          "# _foo"
          "#,_foo"
          "#\t_foo"
          "#\r_foo"
          "#\n_foo")))

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
          [:a [:b 42]] {'my/a (fn [x] [:a x]) 'my/b (fn [x] [:b x])} "#my/a #my/b 42"
          :override {'uuid (constantly :override)} "#uuid \"550e8400-e29b-41d4-a716-446655440000\""
          :override {'inst (constantly :override)} "#inst \"2010-11-12T13:14:15.666-05:00\""))

      (testing "Readers with Default"
        (let [opts {:readers {'my/foo (fn [x] [:foo x])}
                    :default (fn [tag v] [:default tag v])}]
          (are [expected edn] (= expected (edn/read-string opts edn))
            [:foo 42] "#my/foo 42"
            [:default 'my/bar 42] "#my/bar 42")))

      (testing "Unsupplied EOF"
        (are [edn] (p/thrown? (edn/read-string {} edn))
          " "
          ";just a comment\n")))

    (testing "Comments and Discard"
      (are-read-as
        nil ";foo"
        3 ";foo\n3\n5"
        ;; cljs does not consider returns as newlines for comments
        #?(:cljs 5 :default 3) ";foo\r3\n5"
        nil ";#inst \"not-an-instant\""
        nil "#_nope"
        3 "#_nope 3"
        [1 4] "[1 #_ #_ 2 3 4]"
        [1 3] "[1 #_,\t\n\r 2 3]"
        [3] "[#_#{1 2} 3]"
        {:a 1 :c 3} "{:a 1;:b 2\n:c 3}"
        {:a 1 :c 3} "{:a 1 #_:b #_2 :c 3}"
        {:a 1} "{:a #_nope 1}")

      (testing "Discarding Metadata"
        (are-read-as nil "#_ ^:foo {}")
        (are-thrown
          "#_ ^:foo"
          "#_ ^String"
          "#_ ^String \"\""))

      (testing "Discarding Tagged Elements"
        (are-thrown
          "#_ #inst \"not-an-instant\""
          "#_ #foo 0"
          "#_ #foo/bar 0")
        ;; cljs cannot discard tagged elements
        (are [expected edn] #?(:cljs    (p/thrown? (edn/read-string edn))
                               :default (= expected (edn/read-string edn)))
          nil "#_ #inst \"2010-11-12T13:14:15.666-05:00\""
          #{1} "#{#_ #inst \"2010-11-12T13:14:15.666-05:00\" 1}"))

      (testing "Discard Without Element"
        (are-thrown
          "#_"
          "[#_]"
          "#_ #_ 1"))

      (testing "Discarded Tag Handler Throws"
        (let [opts {:readers {'my/boom (fn [_] (throw (ex-info "handler called" {})))}}]
          (is (p/thrown? (edn/read-string opts "[#_ #my/boom 0 42]"))))))

    (testing "Whitespace Between Forms"
      (are-read-as
        [1 2] "[1,2]"
        [1 2] "[1  \r\n,\t2]")

      (testing "Multiple Forms"
        (are-read-as
          'first "first second"
          1 "1 2 3"
          nil "nil\n1"
          1 "\n1"))

      (testing "Commas"
        (are-read-as
          [nil true -1.5 ",,," :foo/bar 'baz/buzz]
          "[nil,true,-1.5,\",,,\",:foo/bar,baz/buzz]")))

    (testing "Unicode"
      (are-read-as
        "אַ גוט יאָר" "\"אַ גוט יאָר\""                     ;; yiddish
        "привет" "\"привет\""                               ;; russian
        'אַ "אַ גוט יאָר"
        'привет "привет"
        :אַ ":אַ גוט יאָר"
        :привет ":привет"))

    (testing "Metadata"
      (let [edn "^String {:a 1}"]
        (is (= {:tag 'String} (meta (edn/read-string edn))))))

    (testing "Malformed Input"
      (testing "Non-String Inputs"
        (are-thrown :foo))

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
          "#{1 2"
          "\"foo")))))
