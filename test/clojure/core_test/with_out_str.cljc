(ns clojure.core-test.with-out-str
  (:require [clojure.test :as t :refer [deftest is]]
            [clojure.core-test.portability
             #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

;; This is part of clojure.test-helpers, but I couldn't figure out how to
;; :require or :use that library.
;; Copied here for now
(defn platform-newlines [s]
  #?(:clj
     (let [nl (System/getProperty "line.separator")]
       (.replace s "\n" nl))
     :cljr
     (let [nl Environment/NewLine] ;;; (System/getProperty "line.separator")]
       (.Replace ^String s "\n" nl)) ;;; .replace, add type hint
     :default
     s))


(when-var-exists clojure.core/with-out-str
  (deftest test-with-out-str
    (is (= (platform-newlines
            (str "some sample :text here" \newline
                 "[:a :b] {:c :d} #{:e} (:f)" \newline))
           (with-out-str
             (println "some" "sample" :text 'here)
             (prn [:a :b] {:c :d} #{:e} '(:f)))))))
