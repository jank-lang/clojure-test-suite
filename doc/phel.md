# Running the Phel tests

## Pre-requisities

- PHP 8.3+ / [Composer](https://getcomposer.org/doc/00-intro.md#installation-linux-unix-macos)

Install Phel:
```
composer install
```

~For convenience Phel version is not pinned for now in `composer.json` so latest `dev-main` version gets used.~

See also [Getting Started guide](https://phel-lang.org/documentation/getting-started/).

## Updating / changing Phel version

Upgrading to latest Phel version (when not pinned in `composer.json`):
```
composer update
```

Requiring specific Phel version, pinning it to `composer.json`:
```
composer require "phel-lang/phel-lang:dev-main#73920b1"
```

Version can also be pinned manually in `composer.json` after which `composer update` will install it:
```javascript
{
    "require": {
        "phel-lang/phel-lang": "dev-main#73920b1"
    }
}
```

## Running the test suite

Run full suite:
```
./vendor/bin/phel test
```
Run specific test:
```
./vendor/bin/phel test test/clojure/core_test/abs.cljc
```

If test runner crashes before producing a report, run the tests with more verbosity using `-v` or `--testdox` flag which may help tracking down the specific test where failure is coming from.

- Phel docs on [running tests](https://phel-lang.org/documentation/testing/#running-tests).
