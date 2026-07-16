(ns clojure.core-test.char
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists char
 (deftest test-char
   (are [expected x] (= expected (char x))
     ;; Assumes ASCII / Unicode
     \space 32
     \@     64
     \A     65
     \A     \A)
   (testing "unicode"
     (testing "2 byte characters are valid"
       (is (= \¡ (char 161))))
     (testing "3 byte characters are valid"
       (is (= \ষ (char 2487))))
     (testing "4+ byte characters throw"
       (is #?(:jank    (= (first "𐅦") (char 65895))
              ;; 65895 is U+10167 ("𐅧"). Runtimes with Unicode-scalar
              ;; characters can represent it directly; JVM Clojure's char is
              ;; a UTF-16 code unit and rejects values above U+FFFF.
              :lg      (= (first "𐅧") (char 65895))
              :lpy     (= (first "𐅧") (char 65895))
              :cljs    (= \ŧ (char 65895))
              :default (p/thrown? (char 65895))))))

   #?(:cljs nil :default (is (p/thrown? (char -1))))
   (is (p/thrown? (char nil)))))
