(ns clojure.core-test.portability
  #?(:lpy (:import time))
  (:require #?(:cljs [cljs.test :as t]
               :default [clojure.test :as t])))

(defmacro when-var-exists [var-sym & body]
  (let [cljs? (some? (:ns &env))
        exists? (boolean (if cljs?
                           ((resolve 'cljs.analyzer.api/resolve) &env var-sym)
                           (resolve var-sym)))]
    (if exists?
      `(do
         ~@body)
      `(println "SKIP -" '~var-sym))))

(defn big-int? [n]
  ;; In CLJS, all numbers are really doubles and integer? and int?
  ;; return true if the fractional part of the double is zero
  #?(:cljs (integer? n)
     :lpy (integer? n)
     :default
     (and (integer? n)
          (not (int? n)))))

(defn sleep [ms]
  (#?(:cljr System.Threading.Thread/Sleep
      :cljs #(js/setTimeout identity %)
      :clj Thread/sleep
      :lpy time/sleep)
   ms))

(defmacro throws?
  [form]
  `(try (do ~form)
        (t/do-report {:type :fail :message nil
                      :expected '~form :actual nil})
        (catch #?(:jank cpp/jank.runtime.object_ref
                  :cljs js/Error
                  :default Exception) e#
          (t/do-report {:type :pass :message nil
                        :expected '~form :actual e#})
          e#)
        #?(:jank (catch cpp/std.exception e#
                   (t/do-report {:type :pass :message nil
                                 :expected '~form :actual (cpp/.what e#)})))))

