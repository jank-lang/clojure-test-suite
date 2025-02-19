(ns clojure.core-test.case
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/case

  ;; Note that this eclectic group of test value types tests the :hashes path in Clojure
  (defn positive-tests
    [x]
    (case x
      sym :sym-result
      :kw :kw-result
      "string" :string-result
      1 :integer-result
      2N :big-integer-result
      3.0 :double-result
      4.0M :big-decimal-result
      #?@(:cljs []
          :default
          [1/2 :ratio-result])
      \a :character-result              ; CLJS reader will convert \a to "a"
      true :boolean-true-result
      false :boolean-false-result
      nil :nil-result
      ##Inf :inf-result
      ##-Inf :negative-inf-result
      #?(:cljs ('(list of syms))        ; CLJS wants to eval the inner list, even though it shouldn't
         :default ((list of syms))) :list-of-syms-result ; if you need to match a list, wrap it in another list
      [:vec :of :kws] :vec-of-kws-result
      {:a :map :of :kws} :map-of-kws-result
      #{:a :set :of :kws} :set-of-kws-result
      (:either :this :or :that) :one-of-multiple-result
      :default))

  ;; this tests the :ints path
  (defn all-nums-tests
    [x]
    (case x
      (0 1) :zero-one
      2 :two
      3 :three
      (4 5) :four-five
      6 :six
      7 :seven
      (8 9) :eight-nine
      :default))

  ;; this tests the :identity path
  (defn all-keywords-tests
    [x]
    (case x
      (:zero :one) :zero-one
      :two :two
      :three :three
      (:four :five) :four-five
      :six :six
      :seven :seven
      (:eight :nine) :eight-nine
      :default))

  (defn negative-tests
    [x]
    (case x
      (range 0 5) :bad-range-result ; range not eval'd. matches 'range, 0, or 5.
      (1 2 3 4) :real-range-result  ; can't include 0 because previous clause
      'foo :quote-foo-result ; reader expands to `(quote foo)`, a list of alternatives
      ##NaN :nan-result
      ;; empty default clause, so will throw if no matches
      ))
  
  (deftest test-case
    (testing "positive test cases"
      (are [x expected] (= expected (positive-tests x))
        'sym :sym-result
        :kw :kw-result
        "string" :string-result
        #?@(:cljs                     ; everything is a double in CLJS
            [1 :integer-result
             1.0 :integer-result
             1N :integer-result
             1.0M :integer-result
             2 :big-integer-result
             2N :big-integer-result
             2.0 :big-integer-result
             2.0M :big-integer-result
             3 :double-result
             3N :double-result
             3.0 :double-result
             3.0M :double-result
             4 :big-decimal-result
             4N :big-decimal-result
             4.0 :big-decimal-result
             4.0M :big-decimal-result]
            :default
            [1 :integer-result
             1N :integer-result ; JVM sees ints and big ints as equal for int-sized values
             1.0 :default ; but doubles and big-doubles aren't the same
             1.0M :default
             2 :big-integer-result
             2N :big-integer-result
             2.0 :default
             2.0M :default
             3 :default
             3N :default
             3.0 :double-result
             3.0M :default ; does discriminate between double and big-decimal
             4 :default
             4N :default
             4.0 :default
             4.0M :big-decimal-result])
        #?@(:cljs []
            :default
            [1/2 :ratio-result])
        \a :character-result      ; CLJS reader will convert \a to "a"
        #?@(:cljs ["a" :character-result] :default []) ; CLJS matches this to `\a`
        true :boolean-true-result
        false :boolean-false-result
        nil :nil-result
        ##Inf :inf-result
        ##-Inf :negative-inf-result
        '(list of syms) :list-of-syms-result
        [:vec :of :kws] :vec-of-kws-result
        '(:vec :of :kws) :vec-of-kws-result ; `case` allows vec pattern to match a list
        #?@(:cljs []
            :default ['[list of syms] :list-of-syms-result])
        {:a :map :of :kws} :map-of-kws-result
        #{:a :set :of :kws} :set-of-kws-result
        :either :one-of-multiple-result
        :this :one-of-multiple-result
        :or :one-of-multiple-result
        :that :one-of-multiple-result
        :match-nuthin :default)

      (are [x expected] (= expected (all-nums-tests x))
        0 :zero-one
        1 :zero-one
        2 :two
        3 :three
        4 :four-five
        5 :four-five
        6 :six
        7 :seven
        8 :eight-nine
        9 :eight-nine
        10 :default
        -1 :default)

      (are [x expected]  (= expected (all-keywords-tests x))
        :zero :zero-one
        :one :zero-one
        :two :two
        :three :three
        :four :four-five
        :five :four-five
        :six :six
        :seven :seven
        :eight :eight-nine
        :nine :eight-nine
        :ten :default
        :negative-one :default))
    
    (testing "negative test cases"
      (are [x expected] (= expected (negative-tests x))
        'range :bad-range-result
        0 :bad-range-result
        1 :real-range-result
        2 :real-range-result
        3 :real-range-result
        4 :real-range-result
        5 :bad-range-result
        'quote :quote-foo-result
        'foo :quote-foo-result)

      (is (thrown? #?(:cljs :default, :clj Exception) (negative-tests ##NaN)))
      (is (thrown? #?(:cljs :default, :clj Exception) (negative-tests :something-not-found))))))
