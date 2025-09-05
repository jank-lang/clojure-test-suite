(ns clojure.core-test.with-out-str
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

;; This is part of clojure.test-helpers, but I couldn't figure out how to :require or :use that library.
;; Copied here for now

#?(:cljr
   (let [nl Environment/NewLine]                                       ;;; (System/getProperty "line.separator")] 
     (defn platform-newlines [s] (.Replace ^String s "\n" nl)))        ;;; .replace, add type hint
   :clj
   (let [nl (System/getProperty "line.separator")] 
    (defn platform-newlines [s] (.replace s "\n" nl)))
   :default   ;; does ClojureScript have its own version?
    (defn platform-newlines [s] s))



(when-var-exists clojure.core/with-out-str
  (deftest test-with-out-str
    (is (= (platform-newlines (str "some sample :text here" \newline
                "[:a :b] {:c :d} #{:e} (:f)" \newline))
           (with-out-str
             (println "some" "sample" :text 'here)
             (prn [:a :b] {:c :d} #{:e} '(:f)))))))
