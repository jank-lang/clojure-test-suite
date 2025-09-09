(ns clojure.string-test.starts-with-qmark
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists str/starts-with?
  (deftest test-starts-with?
    (is (true? (str/starts-with? "" "")))
    #?(:cljs (is (false? (str/starts-with? "" nil)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/starts-with? "" nil))))
	   
	#?(:cljs (is (thrown? :default (str/starts-with? nil "")))
	   :cljr (is (true? (str/starts-with? nil "")))
	   :default (is (thrown? Exception (str/starts-with? nil ""))))
	   
    #?(:cljs (do (is (false? (str/starts-with? "ab" :a)))
                 (is (true? (str/starts-with? ":ab" :a)))
                 (is (false? (str/starts-with? "ab" :b))))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/starts-with? "ab" :a))))
    #?(:cljs (is (true? (str/starts-with? "ab" 'a)))
       :default (is (thrown? #?(:clj Exception :cljr Exception) (str/starts-with? "a" 'a))))
	   
    (is (false? (str/starts-with? "" "a")))
    (is (true? (str/starts-with? "a-test" "")))
    (is (true? (str/starts-with? "֎a-test" "֎")))
    (is (true? (str/starts-with? "a-test" "a")))
    (is (true? (str/starts-with? "a-test" "a-test")))
    (is (false? (str/starts-with? "a-test" "-")))
    (is (false? (str/starts-with? "a-test" "t")))
	
	#?@(:cljs
        [(is (thrown? :default (str/starts-with? 'ab ":a")))
		     (is (thrown? :default (str/starts-with? :ab ":a")))
		     (is (thrown? :default (str/starts-with? 'a/b ":a")))
		     (is (thrown? :default (str/starts-with? :a/b ":a")))]
	    :cljr
        [(is (thrown? Exception (str/starts-with? 'ab ":a")))
		     (is (thrown? Exception (str/starts-with? :ab ":a")))
		     (is (thrown? Exception (str/starts-with? 'a/b ":a")))
		     (is (thrown? Exception (str/starts-with? :a/b ":a")))]
		:default
        [(is (false? (str/starts-with? 'ab "b")))
		     (is (true? (str/starts-with? 'ab "a")))
		     (is (false? (str/starts-with? :ab "b")))
		     (is (false? (str/starts-with? :ab "a")))
		     (is (true? (str/starts-with? :ab ":a")))
		     (is (false? (str/starts-with? 'a/b "b")))
		     (is (true? (str/starts-with? 'a/b "a")))
		     (is (false? (str/starts-with? :a/b "b")))
		     (is (false? (str/starts-with? :a/b "a")))
		     (is (true? (str/starts-with? :a/b ":a")))])))
