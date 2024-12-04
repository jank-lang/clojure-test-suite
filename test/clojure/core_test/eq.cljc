(ns clojure.core-test.eq
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

;; Doesn't test (yet):
;; * Java collections for JVM Clojure
;; * clojure.lang.PersistentQueue
;; * ##Inf and some other special forms
;; * Records

(defn tests [eq]
  (testing "two scalars"
    (are [in ex] (eq in ex)
      nil nil
      true true
      false false
      \a \a
      "green" "green"
      :eggs :eggs
      :and/ham :and/ham
      'one-fish 'one-fish
      'two/fish 'two/fish
      42 42
      3.14 3.14
      3.141592M 3.141592M))

  (testing "two scalars unequal"
    (are [in ex] (not (eq in ex))
      nil false
      true false
      \a \b
      "yellow" "purple"
      :hello :goodbye
      :my/hello 'my/hello
      :my/hello :your/hello
      'one-fish 'two-fish
      'red/fish 'red/coral))

  (testing "a string is not a list of chars"
    (is (not (eq "hello" '(\h \e \l \l \o)))))

  (testing "collections"
    (are [in ex] (eq in ex)
      [] '()
      '() []
      [1 2 3] '(1 2 3)
      '(1 2 3) [1 2 3]
      [0 1 2] (range 3)
      '(0 1 2) (range 3)
      {:a 1 "b" :2 3 \c \d 4} {"b" :2 \d 4 3 \c :a 1}
      #{:a \b "c"} #{\b "c" :a}))

  (testing "collections unequal"
    (are [in ex] (not (eq in ex))
      nil '()
      nil []
      nil #{}
      '() #{}
      '() {}
      [] #{}
      [] {}
      #{} {}
      [1 2 3] #{1 2 3}
      '(1 2 3) #{1 2 3}
      {:a 1 "b" \c} {:a "1" "b" \c}
      #{:a \b "c"} #{\b "d" :a}
      [1 2 3] [3 2 1]
      '(1 2 3) '(3 2 1)
      [\a \b \c] {0 \a 1 \b 2 \c}
      [\a ##NaN] [\a ##NaN]
      #{1.0 2.0 ##NaN} #{1.0 2.0 ##NaN}))

  (testing "sorted collections"
    (are [in ex] (eq in ex)
      {:b 14 :c 15 :a 13} (sorted-map :a 13 :b 14 :c 15)
      (sorted-map-by < 13 :a 14 :b 15 :c) (sorted-map-by > 13 :a 14 :b 15 :c)
      #{6 4 2} (sorted-set 4 2 6)
      (sorted-set-by > 4 2 6) (sorted-set-by < 4 2 6)))

  (testing "nested collections"
    (are [in ex] (eq in ex)
      {#{} ['()]} {#{} ['()]}
      {:just '(:a {:plain [:simple #{:tailor}]})} {:just '(:a {:plain [:simple #{:tailor}]})}
      [1 '(2 3 [4])] (list 1 [2 3 '(4)])))

  (testing "regex"
    ;; Value-equal regex are NOT eq, only identical?
    (is (not (eq #"my regex" #"my regex")))
    (is (let [r #"my regex"
              r' r]
          (eq r r'))))

  (testing "functions"
    ;; identical? functions are eq, but no other functions
    (is (not (eq #(+ 2 %) #(+ 2 %))))
    (is (let [f #(+ 2 %)
              f' f]
          (eq f f'))))

  (testing "variadic eq"
    (is (eq 2 2 2))
    (is (eq "beep" "beep" "beep" "beep"))
    (is (let [my-inc #(+ 1 %)]
          (eq my-inc my-inc my-inc)))
    (is (not (eq '() [] [] (list) {})))
    (is (not (eq 2 2 3 2 2 2 2)))
    (is (not (eq nil \a \a \a))))

  ;; Platform differences
  #?(:clj (testing "jvm"
            (are [in ex eq?] (identical? eq? (eq in ex))
              2 2.0 false
              (float 0.1) (double 0.1) false
              (float 0.5) (double 0.5) true
              1M 1 false
              ;; ratios do not read in CLJS
              22/7 44/14 true
              ;; https://clojure.org/guides/equality notes that sometimes 
              ;; collections with ##NaN are eq
              (list ##NaN) (list ##NaN) true))

     :cljs (testing "cljs"
             (are [in ex eq?] (identical? eq? (eq in ex))
               2 2.0 true
               (float 0.1) (double 0.1) true
               (float 0.5) (double 0.5) true
               1M 1 true
               (list ##NaN) (list ##NaN) false
               ##NaN ##NaN false))))

(when-var-exists clojure.core/=
  (deftest test-eq
    (tests =)
    ;; This is to accomodate a not= JVM bug. See not_eq.cljc
    (testing "If ##NaNs are ="
      (is (not (= ##NaN ##NaN))))))
