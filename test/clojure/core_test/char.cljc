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
              ;; this seems to be an off by one error
              :lpy     (= (first "𐅧") (char 65895))
              :cljs    (= \ŧ (char 65895))
              :default (p/thrown? (char 65895))))))

   #?(:cljs nil :default (is (p/thrown? (char -1))))
   (is (p/thrown? (char nil)))))
