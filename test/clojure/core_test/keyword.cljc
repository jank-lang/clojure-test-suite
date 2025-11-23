(ns clojure.core-test.keyword
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists keyword
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
    (is (nil? (namespace (keyword nil "hi"))))
    (is (= #?(:jank nil :default "") (namespace (keyword "" "hi"))))
    ;; But if name is nil, then maybe we throw or maybe we don't
    #?(; CLJS creates a keyword that isn't
       ; readable (symbol part is null string: ":abc/")
       :cljs
       nil

       :jank
       nil

       :default
       (is (thrown? Exception (keyword "abc" nil))))
  
    #?@(:jank []
        :cljs
        [(is (= :abc/abc (keyword 'abc "abc")))
         (is (= :abc/abc (keyword "abc" 'abc)))
         (is (= :abc/abc (keyword :abc "abc")))
         (is (= :abc/abc (keyword "abc" :abc)))]
        :default
        [(is (thrown? Exception (keyword 'abc "abc")))
         (is (thrown? Exception (keyword "abc" 'abc)))
         (is (thrown? Exception (keyword :abc "abc")))
         (is (thrown? Exception (keyword "abc" :abc)))])))
