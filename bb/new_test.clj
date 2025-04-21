(ns new-test
  "Creates a new test from a template"
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [selmer.parser :as s]
            [selmer.util :as util]))

;;; *, +, !, -, _, ', ?, <, > and =
(defn sym-name->ns-suffix
  "Replace special characters in symbol name to make the final component of the ns name."
  [sym]
  (let [n (name sym)
        ns-sym (-> (if (str/starts-with? n "-")
                     (str/replace-first n "-" "minus")
                     n)
                   (str/replace "*" "-star")
                   (str/replace "+" "-plus")
                   (str/replace "!" "-bang")
                   (str/replace "'" "-squote")
                   (str/replace "?" "-qmark")
                   (str/replace "<" "-lt")
                   (str/replace ">" "-gt")
                   (str/replace "=" "-eq")
                   (str/replace "%" "-percent"))]
    (if (str/starts-with? ns-sym "-")
      (subs ns-sym 1)
      ns-sym)))

(defn ns-suffix->file-name
  "Replace hyphens with underscores to create the file name."
  [ns-name]
  (str/replace ns-name "-" "_"))

(defn ns->resource [nsym]
  (-> nsym namespace-munge (str/replace "." "/")))

(defn new-test
  "Create a new test file for the symbol which is the first command line argument."
  [args]
  (if (zero? (count args))
    (println "Please supply one or more Clojure symbols corresponding to the new tests.")
    (loop [[sym & args] (map symbol args)]
      (when sym
        (let [sym-name (if (namespace sym)
                         (-> sym name symbol)
                         sym)
              base-ns (or (some-> sym namespace symbol)
                          'clojure.core)
              ns-suffix (sym-name->ns-suffix sym-name)
              file-name (namespace-munge ns-suffix)
              dest-dir (format "test/%s_test" (ns->resource base-ns))
              dest-file-name (format "%s/%s.cljc" dest-dir file-name)]
          (if (fs/exists? dest-file-name)
            (println dest-file-name "already exists. No action taken.")
            (do (println "Creating" dest-file-name)
                (fs/create-dirs dest-dir)
                (let [template (slurp "templates/test-template.cljc")]
                  (spit dest-file-name
                        (util/without-escaping
                         (s/render template {:base-ns base-ns
                                             :sym-name sym-name
                                             :ns-suffix ns-suffix
                                             :file-name file-name})))))))
        (recur args)))))
