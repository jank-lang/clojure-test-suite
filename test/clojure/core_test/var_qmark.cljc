(ns clojure.core-test.var-qmark
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(def foo :foo)

(def ^:dynamic i-am-dynamic 3.14)

(when-var-exists clojure.core/defmulti
  ;; This can't be inside `deftest` like `defn` because `defmulti` only returns
  ;; the var on the first invocation.
  (defmulti bar first))

(when-var-exists clojure.core/defprotocol
  (defprotocol MyProtocol))

(when-var-exists clojure.core/var?
  (deftest test-var?
    (testing "things which are vars"
      (are [v] (var? v)
        #'foo               ; locally-defined
        #'var?              ; clojure.core
        #'i-am-dynamic      ; dynamic & local
        #'*assert*          ; dynamic
        #?@(; CLJS `def` doesn't necessarily evaluate to the value of the var:
            :cljs [],
            :default [(def baz)])
        #?@(; CLJS `defn` produces a non-var
            :cljs [],
            :default [(defn qux [] nil)]))

      (when-var-exists clojure.core/defmulti
        (is (var? #'bar)))
      
      (when-var-exists clojure.core/defprotocol
        (is (var? #'MyProtocol))))

    (testing "var-adjacent things"
      (are [not-a-var] (not (var? not-a-var))
        foo
        var?
        i-am-dynamic
        'foo
        'var?
        'i-am-dynamic
        *assert*
        #(+ 1 %)
        (fn baz [x] x)))

    (testing "things which are clearly not vars"
      (are [v] (not (var? v))
        'sym
        `sym
        "abc"
        999
        1.2
        #?@(:cljs [], ; most Clojure dialects support ratios - not CLJS
            :default [2/3])
        \backspace
        nil
        true
        false
        :keyword
        :namespace/keyword
        '(one two three)
        [4 5 6]
        {:7 "8"}
        (zipmap (take 100 (range))
                (cycle ['foo 'bar 'baz 'qux]))
        #{:a :b "c"}))))
