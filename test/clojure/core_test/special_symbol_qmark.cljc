(ns clojure.core-test.special-symbol-qmark
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists special-symbol?
  (deftest test-special-symbol?

    (testing "special symbols"
      (are [arg] (special-symbol? 'arg)
                 &
                 .
                 case*
                 catch
                 def
                 deftype*
                 do
                 finally
                 fn*
                 if
                 let*
                 letfn*
                 loop*
                 new
                 quote
                 recur
                 set!
                 throw
                 try
                 var))

    (testing "not special symbols"
      (are [arg] (not (special-symbol? arg))
                 'a-symbol
                 'a-ns/a-qualified-symbol
                 'defn
                 'import
                 "not a symbol"
                 :k
                 0
                 0.0
                 true
                 false
                 nil))))
