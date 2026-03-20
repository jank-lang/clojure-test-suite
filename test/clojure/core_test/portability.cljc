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

(defmacro thrown?
  "Tests that evaluating `form` throws an exception, without asserting the
  type of exception thrown. Works across all supported Clojure dialects
  (Clojure JVM, ClojureScript, ClojureCLR, Basilisp, bb, jank, etc.) and
  integrates with each dialect's native test result reporting API.

  Prefer this macro over manually written reader conditionals, which risk
  accidentally using dialect-specific symbols as `:default` cases."
  [form]
  `(let [report-success# #?(:lpy (fn [_])
                            :cljs t/report
                            :default t/do-report)
         report-failure# #?(:lpy (partial vswap! t/*test-failures* conj)
                            :cljs t/report
                            :default t/do-report)
         success-opts# (fn [~'error]
                         {:type :pass :message nil
                          :expected '~form :actual ~'error})
         failure-opts# {:type #?(:lpy :failure
                                 :default :fail)
                        :message nil
                        :expected '~form
                        :actual nil}]
     (try
       (do ~form)
       (report-failure# failure-opts#)
       (catch #?(:jank ~'jank.runtime.object_ref
                 ; This is a hack to determine if we're running this macro for Clojure or
                 ; ClojureScript. There doesn't seem to be an official way to check this.
                 :clj ~(if (-> &env :ns some?) 'js/Error 'Throwable)
                 :lpy ~'BaseException
                 :default ~'Exception) e#
         (report-success# (success-opts# e#))
         e#)
       #?(:jank (catch ~'std.exception e#
                  (report-success# (success-opts# (~'.what e#))))))))

