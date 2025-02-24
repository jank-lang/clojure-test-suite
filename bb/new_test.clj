(ns new-test
  "Creates a new test from a template"
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [selmer.parser :as s]
            [selmer.util :as util]))

;;; *, +, !, -, _, ', ?, <, > and =
(defn sym-name->ns-name
  "Replace special characters in symbol name to make an ns-name."
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

(defn ns-name->file-name
  "Replace hyphens with underscores to create the file name."
  [ns-name]
  (str/replace ns-name "-" "_"))

(defn new-test
  "Create a new test file for the symbol which is the first command line argument."
  [args]
  (if (zero? (count args))
    (println "Please supply one or more Clojure symbols corresponding to the new tests.")
    (loop [[sym-name & args] args]
      (when sym-name
        (let [ns-name (sym-name->ns-name sym-name)
              file-name (ns-name->file-name ns-name)
              dest-file-name (str "test/clojure/core_test/" file-name ".cljc")]
          (if (fs/exists? dest-file-name)
            (println dest-file-name "already exists. No action taken.")
            (do (println "Creating" dest-file-name)
                (let [template (slurp "templates/test-template.cljc")]
                  (spit dest-file-name
                        (util/without-escaping
                         (s/render template {:sym-name sym-name
                                             :ns-name ns-name
                                             :file-name file-name})))))))
        (recur args)))))
          


