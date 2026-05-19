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
```

A single file:

```bash
composer test -- test/clojure/core_test/abs.cljc
```

A namespace:

```bash
composer test -- --filter clojure.core-test.abs
```

> The `composer test` script sets `XDEBUG_MODE=off`, which makes startup
> noticeably faster than calling `./vendor/bin/phel test` directly when
> Xdebug is installed system-wide.

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
        "phel-lang/phel-lang": "^0.38"
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
