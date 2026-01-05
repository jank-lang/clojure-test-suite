(ns clojure.core-test.portability)

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
     :jank (cpp/jank.runtime.is_big_integer n)
     :default
     (and (integer? n)
          (not (int? n)))))

(defn sleep [ms]
  (#?(:cljr System.Threading.Thread/Sleep
      :cljs #(js/setTimeout identity %)
      :clj Thread/sleep)
   ms))
