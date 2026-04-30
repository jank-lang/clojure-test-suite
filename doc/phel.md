# Running the Phel tests

## Prerequisites

- PHP 8.4+ & [Composer](https://getcomposer.org/doc/00-intro.md#installation-linux-unix-macos)

Install:
```
composer install
```

Tests live in `test/` (override via `phel-config.php`).

See also [Getting Started guide](https://phel-lang.org/documentation/getting-started/).

## Running the test suite

Full suite:
```
./vendor/bin/phel test
```
Specific test:
```
./vendor/bin/phel test test/clojure/core_test/abs.cljc
```

If runner crashes before report, re-run with `--testdox` or `-v` to locate failing test.

See [Phel testing docs](https://phel-lang.org/documentation/testing/#running-tests).

## Updating Phel version

`composer.json` tracks `dev-main` (latest [phel-lang](https://github.com/phel-lang/phel-lang/) HEAD):
```json
{
    "require": {
        "phel-lang/phel-lang": "dev-main"
    },
    "minimum-stability": "dev"
}
```

Pull latest:
```
composer update phel-lang/phel-lang
```

Pin specific commit (optional):
```
composer require "phel-lang/phel-lang:dev-main#<commit-hash>"
```
