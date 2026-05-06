(ns clojure.core-test.special-symbol-qmark
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists special-symbol?
  (deftest test-special-symbol?

    (testing "special symbols"
      (are [arg] (special-symbol? 'arg)
                 ;; Basilisp does not recognize these as special symbols.
                 #?@(:lpy []
                     :phel [&]
                     :default [&
                               case*
                               new])
                 #?@(:phel []
                     :default
                     [.
                      fn*
                      loop*
                      deftype*
                      set!
                      let*
                      letfn*])
                 var
                 catch
                 def
                 do
                 finally
                 if
                 quote
                 recur
                 throw
                 try))

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
