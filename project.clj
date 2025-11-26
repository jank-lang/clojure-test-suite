(defproject io.github.jank-lang/clojure-test-suite "0.1.0-SNAPSHOT"
  :description "Dialect-independent tests for clojure.core, focused on locking down how Clojure JVM behaves so that other dialects to reach parity."
  :url "https://github.com/jank-lang/clojure-test-suite"
  :license {:name "MPL 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :clr {:main-cmd      ["Clojure.Main"]
        :compile-cmd   ["Clojure.Compile"]}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/clojurescript "1.12.42"]]
  :plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]
            [lein-clr "0.2.2"]])
