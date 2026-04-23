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

1. [Clojure](doc/clojure.md)
2. [ClojureScript](doc/clojurescript.md)
3. [Babashka](doc/babashka.md)
4. [Clojure CLR](doc/clojureclr.md)
5. [Basilisp](doc/basilisp.md)
6. [Phel](doc/phel.md)
