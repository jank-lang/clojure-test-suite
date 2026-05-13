# Running the Phel tests

## Prerequisites

- PHP 8.4+ (`php -v` to check)
- [Composer](https://getcomposer.org/doc/00-intro.md#installation-linux-unix-macos)

Install dependencies:

```bash
composer install
```

Tests live in `test/` (configurable in [`phel-config.php`](../phel-config.php) via `withTestDirs`).

See also the [Phel Getting Started guide](https://phel-lang.org/documentation/getting-started/).

## Running the test suite

Full suite:

```bash
composer test
# or directly:
./vendor/bin/phel test
```

A single file:

```bash
./vendor/bin/phel test test/clojure/core_test/abs.cljc
```

A namespace:

```bash
./vendor/bin/phel test --filter clojure.core-test.abs
```

If the runner crashes before printing a report, re-run with `--testdox` or `-v` to locate the failing test.

See the [Phel testing docs](https://phel-lang.org/documentation/testing/#running-tests).

## Formatting

Format the test sources (uses `withFormatDirs` from `phel-config.php`):

```bash
./vendor/bin/phel format
```

## Updating the Phel version

`composer.json` currently pins a stable release:

```json
{
    "require": {
        "phel-lang/phel-lang": "^0.37"
    }
}
```

Pull the latest matching release:

```bash
composer update phel-lang/phel-lang
```

To track development HEAD instead, switch to `dev-main` and allow dev stability:

```json
{
    "require": {
        "phel-lang/phel-lang": "dev-main"
    },
    "minimum-stability": "dev",
    "prefer-stable": true
}
```

Pin to a specific commit (useful for bisecting upstream regressions):

```bash
composer require "phel-lang/phel-lang:dev-main#<commit-hash>"
```

## Reader conditionals

The shared `.cljc` tests select the Phel branch via `:phel`:

```clojure
#?(:clj  (Integer/MAX_VALUE)
   :cljs js/Number.MAX_SAFE_INTEGER
   :phel php/PHP_INT_MAX)
```

Phel exposes PHP globals under the `php/` namespace and core types under `Phel.Lang.*`
(e.g. `Phel.Lang.ExInfoException`, `Phel.Lang.Collections.Map.PersistentMapInterface`).
See [writing-tests.md](writing-tests.md) for cross-dialect conventions.
