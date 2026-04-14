# Running the Test Suite with ClojureScript

## Setting Up the Node Environment

Make sure you have node 23 or greater.
See the node documentation for how to install node.

Install the node dependencies:

```bash
npm install
```
## Running the ClojureScript Tests

Compile the tests:

```bash
npx shadow-cljs compile test
```
Once tests are compiled they can be ran with:

```bash
node target/js/node-tests.js
```

## Automated Test Running During Development

If you want to autorun the tests during development run the following:

```bash
npx shadow-cljs watch test
```

## Automated Test Running For a Single Namespace

If you only want to autorun specific test files you may run the following:

```bash
npx shadow-cljs watch test --config-merge '{:autorun false}'
```

In another terminal, run the following, multiple namespaces are comma (,)
separated.

```bash
npx nodemon -w target/js taget/js/node-tests.js --test=clojure.core-test.int-questionmark
```

