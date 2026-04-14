# The clojure.core Test Suite

This test suite was created to characterize the behavior of Clojure JVM and provide a compliance test suite for other Clojure dialects.
The test suite tries to cover all of the `clojure.core` functions and some highly used non-core libraries (e.g., `clojure.string`).

Currently, this project is owned by [`jank`](https://github.com/jank-lang/jank), the native code Clojure dialect.
As we build it up and prove jank's readiness, we also create value for the rest of the Clojure community.
As it currently stands, jank isn't able to run `clojure.test` yet, so we're just focusing on building out the test cases for now.

## How To Contribute

Anyone with Clojure knowledge can help out!

Read the document titled [Writing Tests](doc/writing-tests.md) for more detailed information about how to contribute tests.

## Setting Up Dialect-Specific Environments and Running the Tests

See these documents for how to set up individual dialect-specific environments and run the tests.

<<<<<<< HEAD
For a one-off run, you can use the following:

```bash
lein test
```

However, during development, you can use `test-refresh` to automatically re-run
the test suite whenever you save new changes.

```bash
lein test-refresh
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
- `ClojureCLR`: `dotnet tool install --global Clojure.Main --version 1.12.3-alpha4`
- `cljr`: `dotnet tool install --global Clojure.Cljr --version 0.1.0-alpha6`

```bash
cljr -X:test
```
=======
1. [Clojure](clojure.md)
2. [ClojureScript](clojurescript.md)
3. [Babashka](babashka.md)
4. [Clojure CLR](clojureclr.md)
5. [Basilisp](basilisp.md)
>>>>>>> 9839af4 (Move dialect-specific instructions from README.md to separate docs)

## Running the Basilisp tests

### Pre-requisites
- Python 3 / `pip`

You can install Python and `pip` using a tool such as [pyenv](https://github.com/pyenv/pyenv).
With `pip` installed, you can install Basilisp:

```bash
pip install -U pip
python -m venv .venv
source .venv/bin/activate
pip install .
```

Tests can be run using the Babashka task:

```bash
source .venv/bin/activate
bb test-lpy
deactivate
```

## Babashka Tasks

The Clojure Test Suite uses Babashka tasks to provide command line tooling to create new tests and run the test suite in various dialect-specific environments.
You can find out the set of Babashka tasks supported at any point by running

```bash
~$ bb tasks
The following tasks are available:

test-jvm  Runs JVM tests
test-bb   Runs bb tests
test-cljs Runs CLJS tests
new-test  Creates new test for the Clojure symbols named by <args>. Unqualified symbols assume clojure.core
nrepl     Starts an nrepl server on port 1339 using an .nrepl-port file
```

Currently, there are tasks to run the Clojure JVM and Clojurescript test suites. Another task, `new-test`, allows you to easily create new test files that have all the standard naming conventions already applied.

See the document titled [Writing Tests](doc/writing-tests.md) for more information about how to write tests.
See the dialect-specific documents linked previously for more information on how to set up an environment supporting a dialect and run tests for it.
