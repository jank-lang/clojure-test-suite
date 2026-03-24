(ns {{base-ns}}-test.{{ns-suffix}}
  (:require {% if not base-ns = "clojure.core" %}{{base-ns}}
            {% endif %}[clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists {% if not base-ns = "clojure.core" %}{{base-ns}}/{% endif %}{{sym-name}}
  (deftest test-{{sym-name}}
    ;; `testing` sections are optional, depending on how you want to
    ;; structure your tests. If you have a lot of tests and they group
    ;; together in subgroups, then use `testing`. The `testing` form
    ;; can also be a nice way to group tests that only apply to a
    ;; subset of Clojure implementations. These can then be guarded by
    ;; reader conditionals.
    (testing "section name"
      (is (= 1 0)))

    ;; Test cases that expect exceptions should use the `p/thrown?`
    ;; multimethod from the portability namespace, rather than the `thrown?` 
    ;; method provided by `clojure.test`.
    ;;
    ;; Prefer this macro over manually written reader conditionals, which
    ;; risk accidentally using dialect-specific symbols as `:default` cases.
    (testing "exception cases"
      (is (p/thrown? (/ 1 0))))))
