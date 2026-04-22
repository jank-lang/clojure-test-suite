# Writing Tests for Clojure Test Suite

Welcome to the Clojure Test Suite.

This document describes how to contribute tests to the suite.

## Clojure Test Suite Goals

This test suite was created to characterize the behavior of Clojure JVM and provide a compliance test suite for other Clojure dialects.
The test suite tries to cover all of the `clojure.core` functions and some highly used non-core libraries (e.g., `clojure.string`).
Using the test suite, a Clojure dialect can validate that its implementation behaves similarly to the Clojure JVM dialect.
Some differences between dialects are expected and inevitable.
Where valid, expected differences exist between dialects, the Clojure Test Suite uses conditional reader macros to modify the test suite slightly for each implementation.

## Setting Up Your Clojure Test Suite Environment

Ideally, when writing tests, you'll do your own private testing of the test (is that redundant?) on your local machine.
To do that, you'll need to set up your environment for each of the various implementations.
Follow the links below for more detailed information about how to install each.
Try to install at least three environments, if not all of them.
This will help you catch obvious differences and help you conditionalize your tests appropriately.
If possible, install them all and run the tests against each dialect before submitting a PR.

1. [Clojure](clojure.md)
2. [ClojureScript](clojurescript.md)
3. [Babashka](babashka.md)
4. [Clojure CLR](clojureclr.md)
5. [Basilisp](basilisp.md)

## How to Write a New Test

Step-by-step of selecting a function, claiming it, creating a new test, modifying the template, creating a PR.

Assumptions:
1. You know how to use Clojure.
2. You know how to use Git and Github.
3. You know how to submit a PR.

### Selecting a Function to Test

First, you'll need to select a Clojure function to test.
You can select a currently-untested function by browsing the [Issues section](https://github.com/jank-lang/clojure-test-suite/issues) of the Clojure Test Suite project on Github.
Look for issues that are named with a Clojure function (e.g., `clojure.core/foo`).
When a PR is merged it adds tests for a given function and the corresponding issue will be closed.

### Claiming

While tests for a given function are being worked on, the corresponding issue remains open on Github.
If you decide to work on an issue, add a simple comment to the issue on Github indicating that you're claiming it (I just write "Claiming.").
This helps prevent multiple people from wasting time writing tests for the same functions at the same time.
There's no need to add a comment when you're done.
Just submit a PR and the issue will be closed when the PR is merged.
If you see that a function is already claimed, choose another.
If you see that the function was claimed a long time ago and hasn't made any progress (say more than a month), add a comment to the issue and ask whether the person who claimed it initially is still working on it.
The person may have gotten busy and just hasn't had time to work on it.
If you have any questions, you can ask on the `#clojure-test-suite` Slack channel (see below).

### Create a New Git Branch

While you could submit multiple tests for multiple functions in one large PR, it's helpful for reviewers to consider tests for one function at a time.
Unless you are testing a group of functions that very naturally want to be tested together, create separate Git branches and PRs for each function.
This way, reviewers can consider them separately and if there is a problem with one test, it won't block the merging of other tests.

### Create a New Test Using the Standard Template

If you've installed Babashka (and you really should have installed Babashka), you can create a new test using

```
$ bb new-test <function-name>
```

Where `<function-name>` is either something like `sort-by` or `clojure.core/sort-by`.
If the namespace is `clojure.core`, then you can elide it and just write the function name.
The `new-test` task will create a new file in `test/clojure/core_test/<function-name>`.
Note that if `<function-name>` contains non-alphanumeric symbols (or "-", which is technically numeric), the filename will use the following mapping:

| Function Name Character | File Name Replacement |
| :---: | :--- |
| - | "minus" if at the start of a word, otherwise "_" |
| * | "star" |
| + | "plus" |
| ! | "bang" |
| ' | "squote" |
| ? | "qmark" |
| < | "lt" |
| > | "gt" |
| = | "eq" |
| % | "percent" |

Thus, the file containing the `<=` test, for example, will be located in `test/clojure/core_test/lt_eq.cljc`.

Note: Many of these characters are considered special by shells.
You will have to put quotes of various types around them or otherwise escape the characters to prevent the shell from misinterpreting them.

### Write Your Test

At this point, you've created a file for your new tests and it contains boilerplate.
Fire up your favorite editor and create appropriate tests for the function you have selected.
See [Writing a Good Test](#writing-a-good-test), below, for more suggestions on how to craft good tests.

### Run Your New Test

Now that you've crafted a new set of tests, you need to run the test suite against some Clojure dialects to ensure that they pass the way you expect them to.
The best way to do this is using Babashka tasks.
There are Babashka tasks for running the tests under multiple environments:

```bash
$ bb test-jvm        # run tests under Clojure JVM
$ bb test-cljs       # run tests under ClojureScript on Node.js
$ bb test-bb         # run tests under Babashka
$ bb test-lpy        # run tests under Basilisp
```

Ideally, you should run all three of these *before* submitting a PR.
This helps prevent the PR from failing during CI testing.
The Clojure Test Suite CI testing runs the tests in Clojure, ClojureScript, Babashka, and ClojureCLR environments.

### Create commits

Your commit message should be similar to "Add tests for `foo`".
Don't get creative here.
Keep it short and sweet.
This text will also flow into the PR title by default.

### Create a PR on Github

After pushing your new branch containing commits that implement your test to your forked project on Github, create a new PR.
Add a "Closes #xxx" comment with the appropriate issue number in the PR comment section.
This helps ensure the correct issue gets closed when your PR is merged.

### Monitor Github for Reviewer Comments

After your PR is submitted, reviewers might provide comments, ask questions, or suggest changes.
Make sure you respond quickly so that your PR doesn't stall out.

That's it.
Now that you've submitted your first test, pick a new function, claim it on Github, and repeat the process.
Keep going until we've got tests for all of `clojure.core` and other popular standard libraries (e.g., `clojure.string`).

## Writing a Good Test

Here are some things to do and think about when you're writing tests.

1. Make sure you cover all the arities of the function.
   It's easy to forget about arities that aren't used very much (e.g., `<` has a single-arity case).
   You don't need to cover invalid arities; the runtime checks that for us.
2. Think about the functionality implemented by this function.
   Fundamentally, what is this function supposed to do?
3. Look up the function at [clojuredocs.org](clojuredocs.org).
   Read through the documentation string to make sure you understand exactly what the function does and what it *promises*.
   You need to ensure that you test the *promise*.
4. Review the examples on the [clojuredocs.org](clojuredocs.org) page for the function.
   That might give you some ideas of tests that would exercise the functionality.
5. Write test cases that exercise that base functionality.
   These are the common cases.
6. Think about the complete domain of the function.
   What values are legal?
   Does the function take an integer or a general number?
   If a general number, you should probably write tests that specifically exercise longs, doubles, big integers, big decimals, and rationals.
7. Think about oddball-but-legitimate cases.
   These are the edge cases.
    - What if you pass an empty sequence into the function?
    - What if you pass `nil` as a sequence?
    - What if you pass a rational number (e.g., `1/3`) to a function that takes a number?
    - How about `##Inf` to a number that takes a double?
    - What about passing the empty string (`""`) to a string function?
    - Think about off-by-one errors that could occur in the implementation.
      Try to generate tests that force those cases.
8. Think about negative test cases, things that should clearly generate an error or an exception.
   Put these into a separate `testing` section within the test.
   This makes it easier to remove these tests later if we need to.
   There is going to be more variation between implementations for these tests.
    - What if you pass a negative integer where a positive integer is expected?
    - If you pass an index into a vector, what happens if the index is zero or past the end of the vector?
    - What if you pass a string or keyword where a sequence is expected?
9. Keep your tests small.
   Try to only test one function in a single file.
   You'll inevitably need to use other functions to do this, but use as few as is possible.
   The more you use, the more functionality must be working correctly in an implementation that is running your tests.
   As new Clojure implementations are built, it's useful to be able to start running tests as soon as possible.
   There's no right number for the maximum number of dependencies.
   Use your best judgment but fewer is almost always better
10. When testing for an exception, use the `clojure.core-test.portability/thrown?` macro.
   The standard test template helpfully requires this name space aliased to `p` (thus you can use `p/thrown?`).
   Various Clojure dialects throw slightly different exceptions and some platforms are more limited in what they can support.
   This macro handles the differences and makes the test code more portable.
11. Put tests into separate `testing` sections where it makes sense.
    I often make a separate section for each specific arity and for edge cases and negative cases.
    Sometimes, if a specific platform doesn't support a specific function, you can isolate all those tests to a `testing` section and then use the conditional reader macros to include or exclude it only for specific platforms.
    For example, CLJS doesn't support rational numbers, so if you do tests with rationals, it can be helpful to put those in a separate `testing` form and then exclude those tests for CLJS.
12. Let your mind go and try to consider what could go wrong with this function if it was implemented incorrectly, by someone just concerned about the "happy path" who wasn't thinking deeply about the corner cases.
    Write tests to validate those corner cases.
   
## Things to Avoid

There are a few things that will cause your PR to be rejected.
Be aware of these issues.

1. Don't use AI to write your tests.
   AI often performs poorly when trying to reason about edge cases and negative cases.
   We want real humans thinking about these test cases.
2. Don't use generative testing (e.g., `clojure.test.check`).
   We think generative testing is great, but we're not ready to adopt it in the Clojure Test Suite at this time.
3. Size your test data to be reasonable.
   You probably don't need to use a sequence of 10000 items to test something; a sequence of 10 items will probably suffice.
   Testing larger sequences can increase runtime, and it all adds up.
   For integer values, you can often do most of your testing with `-1`, `0`, and `1`.
4. Remove any randomness from your tests.
   A test should deterministically pass or fail.
   The only time where you might test something in a probabilistic manner is when the function under test is inherently random.
   For instance, see the tests for `rand`, `rand-nth`, and `rand-int`.
   These tests check that the functions under test vary from call to call (i.e., are not constant).
   They do so in a way that is *extremely* unlikely to fail, but theoretically could (if, for instance, the function was very constant-ish).
   This is as close to non-deterministic as you ever want to get.
   If your tests are non-deterministic, the CI system and people using the test suite will get phantom errors from time to time.
   These errors are extremely difficult to track down.
5. Don't over test.
   You don't need to try every possible test case exhaustively.
   If a first case is covered by a second, you can generally remove the second.
   That said, there are some times where what seems like over-testing can exercise a different code path.
   For instance, we found a bug in ClojureCLR related to using `<` and `>` as the comparison function in `sort-by`.
   We were already testing `sort-by` with `compare` as the comparison function, so those tests using `<` and `>` should have been redundant, but they exercised a different code path in ClojureCLR and caught a bug.
   So, use your discretion.
   It's better to *SLIGHTLY* over-test than to under-test.
   Feel free to make test assertions you plan to delete later. 
   Test them locally to satisfy your curiosity about Clojure's behavior.
   Then delete them, or keep them if they reveal a bug in one or more dialects.
   If something feels fishy, look into it, but if it's a non-standard or redundant test then drop it before final PR review.
6. Avoid getting creative with the overall test format.
   If you use the `new-test` Babashka task (command: `bb new-test`), a new test in the correct format will be created for you, and you just have to fill in the actual test cases.
   This keeps all the tests consistent.
   
## Handling Differences Between Clojure Dialects

## Resources

* `#clojure-test-suite` on [Clojurians Slack](https://clojurians.slack.com)
* [clojuredocs.org](https://clojuredocs.org)
