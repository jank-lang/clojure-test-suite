(ns clojure.core-test.str
  (:require [clojure.test :as t :refer [are deftest is]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists str
  (deftest test-str
    (are [expected x] (= expected (str x))
      "0"            0
      "1"            1
      "-1"           -1
      #?@(:cljs
          ["0"          0.0
           "1"          1.0
           "-1"         -1.0
           "0"          0.00000
           "0"          (float 0.0)
           "1"          (float 1.0)
           "-1"         (float -1.0)
           "0"          (double 0.0)
           "1"          (double 1.0)
           "-1"         (double -1.0)]
          :cljr
          ["0"          0.0
           "1"          1.0
           "-1"         -1.0
           "0"          0.00000
           "0"          (float 0.0)
           "1"          (float 1.0)
           "-1"         (float -1.0)
           "0"          (double 0.0)
           "1"          (double 1.0)
           "-1"         (double -1.0)]
          :default
          ["0.0"          0.0
           "1.0"          1.0
           "-1.0"         -1.0
           "0.0"          0.00000
           "0.0"          (float 0.0)
           "1.0"          (float 1.0)
           "-1.0"         (float -1.0)
           "0.0"          (double 0.0)
           "1.0"          (double 1.0)
           "-1.0"         (double -1.0)])
      "Infinity"     ##Inf
      "-Infinity"    ##-Inf
      "NaN"          ##NaN
      "0"            0N
      "1"            1N
      "-1"           -1N
      #?@(:cljs
          ["0"          0.0M
           "1"          1.0M
           "-1"         -1.0M]
          :default
          ["0"          0/2
           "1/2"        1/2
           "-1/2"       -1/2
           "0.0"        0.0M
           "1.0"        1.0M
           "-1.0"       -1.0M])
      
      ""             nil
      #?(:cljr "True" :default "true")     true
      #?(:cljr "False" :default "false")   false
      "a string"     "a string"
      "0"            "0"
      "1"            "1"
      "-1"           "-1"
      "{:a :map}"    {:a :map} ; keep this one item because it's unordered
      "#{:a-set}"    #{:a-set} ; keep this one item because it's unordered
      "[:a :vector]" [:a :vector]
      "(:a :list)"   '(:a :list)
      "0"            \0
      "1"            \1
      "A"            \A
      " "            \space
      ":a-keyword"   :a-keyword
      ":0"           :0
      ":1"           :1
      ":-1"          :-1
      "a-sym"        'a-sym)

    ;; No arg and var arg versions
    (is (= "" (str)))
    (is (= "astringwithnospaces" (str "a" "string" "with" "no" "spaces")))))
