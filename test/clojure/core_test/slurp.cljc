(ns clojure.core-test.slurp
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/slurp

  ;; DOCSTRING:
  ;; clojure.core/slurp
  ;; [f & opts]
  ;; Opens a reader on f and reads all its contents, returning a string.
  ;; See clojure.java.io/reader for a complete list of supported arguments.

  ;; p/with-temp-file binds a new temporary filename to the variable in the
  ;; following vector, then deletes that file when the form exits.
  (defn slurped-contents
    "Write `content` to a fresh temporary file and return the contents of
     that file as read back by `slurp`."
    [content]
    (p/with-temp-file [path]
      (spit path content)
      (slurp path)))

  (deftest test-slurp
    (testing "returns the contents of a file as a string"
      (p/with-temp-file [path]
        (spit path "hello, world")
        (is (= "hello, world" (slurp path)))
        (is (string? (slurp path)))))

    (testing "returns an empty string for an empty file"
      (p/with-temp-file [path]
        (is (= "" (slurp path)))))

    (testing "returns the whole contents, unaltered"
      (are [content] (= content (slurped-contents content))
        ""
        " "
        "abc"
        "a\nb\nc"
        "a\nb\nc\n"                         ; trailing newline is preserved
        "\n"
        "\n\n\n"
        "a\n\nb"                            ; blank lines are preserved
        "  padded  "
        "a\tb"
        "line 1\r\nline 2"                  ; line endings are not normalized
        "(a b c)"                           ; contents are not read as forms
        "[1 2 3]"
        "\"quoted\""))

    (testing "reads contents larger than any internal buffer"
      (let [single-line (apply str (repeat 1000 "0123456789"))
            multi-line  (apply str (repeat 1000 "0123456789\n"))]
        (is (= single-line (slurped-contents single-line)))
        (is (= multi-line (slurped-contents multi-line)))))

    (testing "reads multi-byte characters"
      (is (= "héllo → 世界" (slurped-contents "héllo → 世界"))))

    (testing "reads from an already open reader"
      (is (= "already open" (slurp (p/string-reader "already open"))))
      (is (= "" (slurp (p/string-reader ""))))
      (is (= "a\nb" (slurp (p/string-reader "a\nb")))))

    (testing "supports the :encoding option"
      (p/with-temp-file [path]
        (spit path "héllo → 世界")
        (is (= "héllo → 世界" (slurp path :encoding "UTF-8")))))

    (testing "a file can be slurped more than once"
      (p/with-temp-file [path]
        (spit path "again and again")
        (is (= "again and again" (slurp path)))
        (is (= (slurp path) (slurp path)))))

    (testing "each call reads only the file it is given"
      (p/with-temp-file [first-file]
        (p/with-temp-file [second-file]
          (spit first-file "first")
          (spit second-file "second")
          (is (= "first" (slurp first-file)))
          (is (= "second" (slurp second-file)))
          (is (= "first" (slurp first-file))))))

    (testing "nests with itself"
      ;; One file holds the path of another, so reading the second file's
      ;; contents requires slurping the first file.
      (p/with-temp-file [pointer]
        (p/with-temp-file [target]
          (spit target "the target contents")
          (spit pointer target)
          (is (= "the target contents" (slurp (slurp pointer))))))
      ;; a file which contains its own path slurps to itself, so nesting the
      ;; call reads the same contents twice over
      (p/with-temp-file [path]
        (spit path path)
        (is (= path (slurp (slurp path))))))

    (testing "exception cases"
      (is (p/thrown? (slurp "/this/directory/does/not/exist/slurp.txt")))
      (is (p/thrown? (slurp "")))
      (is (p/thrown? (slurp nil))))))
