# Running the Test Suite with Clojure

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
