# The clojure.core test suite
This is a set of tests for Clojure's core standard library. Its purpose is not
only to draw clear boundaries around Clojure JVM's behavior, but also to serve
as a compliance suite for other Clojure dialects.

Currently, this project is owned by jank, the native Clojure dialect. As we
build it up and prove jank's readiness, we also create value for the rest of the
Clojure community. As it currently stands, jank isn't able to run `clojure.test`
yet, so we're just focusing on building out the test cases for now.

## How to contribute
Anyone with Clojure knowledge can help out!

Check out the latest progress and the steps for helping out here: https://github.com/jank-lang/clojure.core-test/issues/1

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
## Running the clojurescript tests

First, make sure you haev node 23 or greater.

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
npx shadow-cljs watch app --config-merge '{:autorun false}'
```

In another terminal, run the following, multiple namespaces are comma (,)
separated.

```bash
npx nodemon -w target/js taget/js/node-tests.js --test=clojure.core-test.int-questionmark
```

## Babashka Tasks

You can see which Babashka tasks are available with:
```bash
bb tasks
```

Currently, there are tasks to run the Clojure JVM and Clojurescript test suites.

Another task, `new-test`, allows you to easily create new test files
that have all the standard naming conventions already applied. If you
wanted to test a function named `clojure.core/foo`, for instance, you
would type:

```bash
bb new-test foo
```

will create a new file named `foo.cljc` in the test namespace. The
test file will look like the following:

```
(ns clojure.core-test.foo
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/foo
  (deftest test-foo
    (testing "section name"
      ;; assertions
      ;; (is/are ... )
      )
    (testing "section name"
      ;; more assertions
      ;; (is/are ... )
      )))
```

Simply fill in test assertions and you're off and running.
