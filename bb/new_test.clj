(ns new-test
  "Creates a new test from a template"
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [selmer.parser :as s]
            [selmer.util :as util]))

;;; This whole process is a bit convoluted, so here's a short glossary:
;;; fut = "function under test"; the function we're creating the new test for.
;;; fut-sym = isolated symbol name of the fut, with no namespace
;;; fut-ns = namespace of the fut itself, with no symbol name
;;; thus, fut = fut-ns/fut-sym
;;; no-specials-sym = fut-sym with special chars replaced with words
;;; test-ns = test namespace
;;; test-dir = directory the test source code lives in
;;; test-filename = name of the actual test file
;;; test-path = the path to the file (basically test-dir + test-filename)
;;; deftest-name = the name of the test in the `deftest` form

(defn replace-specials
  "Replace special characters (-, *, +, !, ', ?, <, >, =, and %) in
  symbol name with filename safe alphabetic names. The '-' character
  has some special handling: if it's the first character or is used in
  combination with '>', it's expanded to 'minus', otherwise it's left
  alone."
  [sym]
  (let [n (name sym)
        no-specials-sym (-> (if (str/starts-with? n "-")
                              (str/replace-first n "-" "minus")
                              n)
                            (str/replace #"-$" "-minus")
                            (str/replace "->" "-minus-gt") ; cond->, some->, as->, etc.
                            (str/replace "*" "-star")
                            (str/replace "+" "-plus")
                            (str/replace "!" "-bang")
                            (str/replace "'" "-squote")
                            (str/replace "?" "-qmark")
                            (str/replace "<" "-lt")
                            (str/replace ">" "-gt")
                            (str/replace "=" "-eq")
                            (str/replace "%" "-percent"))]
    (if (str/starts-with? no-specials-sym "-")
      (subs no-specials-sym 1)
      no-specials-sym)))

(defn ns->path
  "Converts a namespace like 'clojure-foo.bar to a path like \"clojure_foo/bar\"."
  [ns]
  ;; note: `namespace-munge` is in `clojure.core` and converts '-' to '_'
  (apply fs/path
         (-> ns namespace-munge (str/split #"\."))))

(defn format-docstring-comment
  "Returns a string that comprises the docstring comment (indented with
  embedded newlines), or `nil` if the var for the `fut` couldn't be
  found."
  [fut]
  (let [indent 2
        indented-comment (str (str/join (repeat indent " ")) ";; ")]
    (if-let [fut-var (try (requiring-resolve fut)
                          (catch Exception _ nil))]
      (let [{:keys [arglists doc]} (meta fut-var)]
        (str/join (System/lineSeparator)
                  (concat [(str indented-comment "DOCSTRING:")]
                          [(str indented-comment fut)]
                          (map (fn [x]
                                 (str indented-comment x))
                               arglists)
                          (map (fn [x]
                                 (str indented-comment (str/triml x)))
                               (str/split-lines doc)))))
      (str indented-comment "DOCSTRING: unable to locate"))))

(defn new-test
  "Create a new test file for the symbol which is the first command line argument."
  [args]
  (if (zero? (count args))
    (println "Please supply one or more Clojure symbols corresponding to the new tests.")
    (loop [[fut & args] (map symbol args)] ; fut = function under test
      (when fut
        (let [fut-sym (-> fut name)
              fut-ns (or (namespace fut)
                         "clojure.core")
              fut (symbol fut-ns fut-sym)
              no-specials-sym (replace-specials fut-sym)
              test-ns (str fut-ns "-test." no-specials-sym)
              test-dir (fs/path "test" (-> fut-ns
                                           (str "-test")
                                           ns->path))
              test-filename (str (namespace-munge no-specials-sym) ".cljc")
              test-path (fs/path test-dir test-filename)
              deftest-name (str "test-" fut-sym)
              docstring-comment (format-docstring-comment fut)]
          (if (fs/exists? test-path)
            (println (str test-path) "already exists. No action taken.")
            (do (println "Creating" (str test-path))
                (fs/create-dirs test-dir)
                (let [template (slurp "templates/test-template.cljc")]
                  (spit (str test-path)
                        (util/without-escaping
                         (s/render template
                                   {:fut fut
                                    :fut-sym fut-sym
                                    :fut-ns fut-ns
                                    :no-specials-sym no-specials-sym
                                    :test-ns test-ns
                                    :test-dir test-dir
                                    :test-filename test-filename
                                    :test-path test-path
                                    :deftest-name deftest-name
                                    :docstring-comment docstring-comment})))))))
        (recur args)))))
