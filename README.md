# The clojure.core test suite
This is a set of tests for Clojure's core standard library. Its purpose is not
only to draw clear boundaries around Clojure JVM's behavior, but also to serve
as a compliance suite for other Clojure dialects.

Currently, this project is owned by [`jank`](https://github.com/jank-lang/jank), the native Clojure dialect. As we
build it up and prove jank's readiness, we also create value for the rest of the
Clojure community. As it currently stands, jank isn't able to run `clojure.test`
yet, so we're just focusing on building out the test cases for now.

## How to contribute
Anyone with Clojure knowledge can help out!

Check out the latest progress and the steps for helping out here: https://github.com/jank-lang/clojure-test-suite/issues/1

## Running the tests

Note: You can also run tests with Babashka tasks. See below.

For a one-off run, you can use the following:

```bash
$ lein test
```

However, during development, you can use `test-refresh` to automatically re-run
the test suite whenever you save new changes.

```bash
$ lein test-refresh
```

## Running the ClojureScript tests

First, make sure you have node 23 or greater.

Install the node dependencies:

```bash
npm install
```

Compile the tests:

```bash
npx shadow-cljs compile test
```

Once tests are compiled they can be ran with:

```bash
node target/js/node-tests.js
```

### Automated test running during development

If you want to autorun the tests during development run the following:

```bash
npx shadow-cljs watch test
```

### Automated test running for a single namespace

If you only want to autorun specific test files you may run the following:

```bash
npx shadow-cljs watch test --config-merge '{:autorun false}'
```

In another terminal, run the following, multiple namespaces are comma (,)
separated.

```bash
npx nodemon -w target/js taget/js/node-tests.js --test=clojure.core-test.int-questionmark
```

## Running the ClojureCLR tests

### Pre-requisites
- [dotnet](https://dotnet.microsoft.com/en-us/download)
- `ClojureCLR`: `dotnet tool install --global Clojure.Main --version 1.12.2`
- `cljr`: `dotnet tool install --global Clojure.Cljr --version 0.1.0-alpha6`

```bash
cljr -X:test
```

## Babashka Tasks

You can see which Babashka tasks are available with:
```bash
~$ bb tasks
The following tasks are available:

test-jvm  Runs JVM tests
test-cljs Runs CLJS tests
new-test  Creates new test for the Clojure symbols named by <args>. Unqualified symbols assume clojure.core
nrepl     Starts an nrepl server on port 1339 using an .nrepl-port file
```

Currently, there are tasks to run the Clojure JVM and Clojurescript test suites.

Another task, `new-test`, allows you to easily create new test files
that have all the standard naming conventions already applied. If you
wanted to test a function named `clojure.core/foo`, for instance, you
would type:

```bash
bb new-test clojure.core/foo
# or
bb new-test foo

# For other clojure namespaces, specify the ns, like:
bb new-test clojure.string/includes?
```

The test file will look like the following:

```clojure
(ns clojure.core-test.foo
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists foo
  (deftest test-foo
    ;; `testing` sections are optional, depending on how you want to
    ;; structure your tests. If you have a lot of tests and they group
    ;; together in subgroups, then use `testing`. The `testing` form
    ;; can also be a nice way to group tests that only apply to a
    ;; subset of Clojure implementations. These can then be guarded by
    ;; reader conditionals.
    (testing "section name"
      (is (= 1 0)))))
```

Simply fill in test assertions and you're off and running.

Note: `new-test` takes care of converting various characters that
might be problematic in file names to expanded versions. For instance,
"?" is converted to "qmark" and "*" is converted to
"star". Thus, you should always provide the name of the `clojure.core`
symbol you want to test, not the file name or other name. You may need
to quote or escape special characters when executing the command in
the shell, however, to prevent the shell from interpreting them before
they are passed to the Babashka task.

The complete set of conversions of characters to names is:
- "*" -> "star"
- "+" -> "plus
- "!" -> "bang"
- "'" -> "squote"
- "?" -> "qmark"
- "<" -> "lt"
- ">" -> "gt"
- "=" -> "eq"
- "%" -> "percent"
