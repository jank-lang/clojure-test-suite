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

;; --- Portable exception multimethod. ---

;; Tests that evaluating `form` throws an exception, without asserting the
;; type of exception thrown. Works across all supported Clojure dialects
;; (Clojure JVM, ClojureScript, ClojureCLR, Basilisp, bb, jank, etc.) and
;; integrates with each dialect's native test result reporting API.
;;
;; Prefer this multimethod over manually written reader conditionals, which
;; risk accidentally using dialect-specific symbols as `:default` cases.".

#?(;; The implementation for CLJS is separate but the intent behind it is the same.
   :cljs nil
   :default
(defmethod #?(:lpy t/gen-assert
              :default t/assert-expr)
  'p/thrown?
  #?(:lpy [form msg _line-num]
     :default [msg form])
  (let [body (drop 1 form)]
    `(let [report-success# #?(:lpy (fn [_])
                              :default t/do-report)
           report-failure# #?(:lpy (partial vswap! t/*test-failures* conj)
                              :default t/do-report)
           success-opts# (fn [~'error]
                           {:type :pass :message '~msg
                            :expected '~form :actual ~'error})
           failure-opts# {:type #?(:lpy :failure
                                   :default :fail)
                          :message '~msg 
                          :expected '~form
                          :actual nil}]
       (try
         ~@body
         (report-failure# failure-opts#)
         (catch #?(:jank ~'jank.runtime.object_ref
                   :clj ~'Throwable
                   :default ~'Exception) e#
           (report-success# (success-opts# e#))
           e#)
         #?(:jank (catch ~'std.exception e#
                    (report-success# (success-opts# (~'.what e#))))))))))

;; The ClojureScript implementation of the portable exception multimethod is
;; slightly special. The `cljs.test/assert-expr` is not a ClojureScript
;; runtime var, it's a Clojure-side compiler multimethod. It lives in the JVM
;; cljs.test namespace and is invoked during macro expansion of cljs.test/is,
;; not at runtime in the JS environment.
#?(:clj
   (try
     (require '[cljs.test])
     (eval '(defmethod cljs.test/assert-expr 'p/thrown?
              [_menv msg form]
              (let [body (drop 1 form)]
                `(try
                   ~@body
                   (cljs.test/report
                     {:type :fail
                      :message '~msg 
                      :expected '~form
                      :actual nil})
                   (catch ~'js/Error e#
                     (cljs.test/report
                       {:type :pass
                        :message '~msg
                        :expected '~form
                        :actual e#})
                    e#)))))
     (catch Exception  _)))

;; --- Portable exception multimethod. ---

