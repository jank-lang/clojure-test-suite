# Running the Test Suite with Basilisp

## Pre-requisites
- Python 3 / `pip`

You can install Python and `pip` using a tool such as [pyenv](https://github.com/pyenv/pyenv).
With `pip` installed, you can install Basilisp:

```bash
pip install -U pip
python -m venv .venv
source .venv/bin/activate
pip install .
```

## Running Tests

Tests can be run using the Babashka task:

```bash
source .venv/bin/activate
bb test-lpy
deactivate
```
