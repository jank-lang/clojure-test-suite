(ns clojure.core-test.keyword
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/keyword
  (deftest test-keyword
    ;; "Symbols begin with a non-numeric character and can contain
    ;; alphanumeric characters and *, +, !, -, _, ', ?, <, > and =
    ;; (other characters may be allowed eventually)."
    ;; 
    ;; "Keywords are like symbols, except: * They can and must begin with a colon, e.g. :fred."
    ;;
    ;; (see http://clojure.org/reader for details)
    ;;
    ;; From https://clojuredocs.org/clojure.core/keyword
    ;; keyword does not validate input strings for ns and name, and may
    ;; return improper keywords with undefined behavior for non-conformant
    ;; ns and name.
  
    (are [expected name] (= expected (keyword name))
      :abc "abc"
      :abc 'abc
      :abc :abc
      :* "*"
      :* '*
      :* :*
      :+ "+"
      :+ '+
      :+ :+
      :! "!"
      :! '!
      :! :!
      :- "-"
      :- '-
      :- :-
      :_ "_"
      :_ '_
      :_ :_
      :? "?"
      :? '?
      :? :?
      :< "<"
      :< '<
      :< :<
      :> ">"
      :> '>
      :> :>
      := "="
      := '=
      := :=
      :abc*+!-_'?<>= "abc*+!-_'?<>=")

    (are [expected ns name] (= expected (keyword ns name))
      :abc/abc     "abc"     "abc"
      :abc.def/abc "abc.def" "abc"
    
      :*/abc "*" "abc"
      :+/abc "+" "abc"
      :!/abc "!" "abc"
      :-/abc "-" "abc"
      :_/abc "_" "abc"
      :?/abc "?" "abc"
      :</abc "<" "abc"
      :>/abc ">" "abc"
      :=/abc "=" "abc"

      :abc.def/* "abc.def" "*"
      :abc.def/+ "abc.def" "+"
      :abc.def/! "abc.def" "!"
      :abc.def/- "abc.def" "-"
      :abc.def/_ "abc.def" "_"
      :abc.def/? "abc.def" "?"
      :abc.def/< "abc.def" "<"
      :abc.def/> "abc.def" ">"
      :abc.def/= "abc.def" "="

      :abc*+!-_'?<>=/abc*+!-_'?<>= "abc*+!-_'?<>=" "abc*+!-_'?<>=")
  
    (is (nil? (keyword nil)))     ; (keyword nil) => nil, surprisingly
    (is (= :abc (keyword nil "abc"))) ; If ns is nil, we just ignore it.
    ;; But if name is nil, then maybe we throw or maybe we don't
    #?(:cljs nil         ; CLJS creates a keyword that isn't
                         ; readable (symbol part is null string: ":abc/")
       :default
       (is (thrown? #?(:clj Exception :cljr Exception) (keyword "abc" nil))))
  
    #?@(:clj
        ;; In Clojure JVM, two arg version requires namespace and
        ;; symbol to be a string, not a symbol or keyword like the one
        ;; arg version.
        [(is (thrown? #?(:clj Exception) (keyword 'abc "abc")))
         (is (thrown? #?(:clj Exception) (keyword "abc" 'abc)))
         (is (thrown? #?(:clj Exception) (keyword :abc "abc")))
         (is (thrown? #?(:clj Exception) (keyword "abc" :abc)))]
	   :cljr
        ;; In Clojure JVM, two arg version requires namespace and
        ;; symbol to be a string, not a symbol or keyword like the one
        ;; arg version.
        [(is (thrown? #?(:cljr Exception) (keyword 'abc "abc")))
         (is (thrown? #?(:cljr Exception) (keyword "abc" 'abc)))
         (is (thrown? #?(:cljr Exception) (keyword :abc "abc")))
         (is (thrown? #?(:cljr Exception) (keyword "abc" :abc)))]
        :default ; + :jank
        ;; IMO, CLJS gets this right
        [(is (= :abc/abc (keyword 'abc "abc")))
         (is (= :abc/abc (keyword "abc" 'abc)))
         (is (= :abc/abc (keyword :abc "abc")))
         (is (= :abc/abc (keyword "abc" :abc)))])))
