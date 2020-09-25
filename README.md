# seancorfield.readme

A simple library that turns examples in your `README.md` file into tests and runs them!

This project follows the version scheme MAJOR.MINOR.COMMITS where MAJOR and MINOR provide some relative indication of the size of the change, but do not follow semantic versioning. In general, all changes endeavor to be non-breaking (by moving to new names rather than by breaking existing names). COMMITS is an ever-increasing counter of commits since the beginning of this repository.

Latest stable release: 1.0.16

## Usage

Add an alias to your `~/.clojure/deps.edn` file:

``` clojure
    :readme {:extra-deps {seancorfield/readme {:mvn/version "1.0.16"}}
             :main-opts ["-m" "seancorfield.readme"]}
```

Then the most basic usage is:

    clojure -A:readme

This turns `README.md`'s examples into tests in `src/readme.clj`, loads and runs them, and then deletes that generated file (line numbers in failures should match line numbers in your original `README.md` file).

You can optionally provide a different file path for the readme and for the generated file:

    clojure -A:readme test/seancorfield/readme_example.md src/generated_test.clj

> Note: The output file path must be on your classpath so that the generated namespace can be `require`'d. The generated file will be deleted after running the tests (unless it cannot be `require`'d due to syntax errors, when it will be left in place for you to debug).

If your `README.md` file contains a REPL session (using a `user=>` prompt) such as:

    ```clojure
    user=> some-expression
    result-1
    user=> another-expression
    result-2
    ```

This will generate tests of the form:

``` clojure
(deftest readme-N ; N is the line number
  (is (= result-1 some-expression))
  (is (= result-2 another-expression)))
```

If your `README.md` file contains code blocks of the form:

    ```clojure
    some-expression
    another-expression
    => result
    ```

This will generate tests of the form:

``` clojure
(deftest readme-N ; N is the line number
  (is (= result (do some-expression another-expression))))
```

Any additional code, without `user=>` or `=>`, will be added to the generated test namespace as-is with no direct test. This allows setup code to be shown in the `README.md` file, followed by specific tests.

Each `clojure` code block will become a standalone test (if it contains `user=>` or `=>`). The tests may be executed in any order (by `clojure.test/run-tests`). Expressions that are not considered to be parts of any tests will be executed in order when the generated test namespace is loaded (by this `readme` library).

If you wish to add Clojure-formatted code to your README that is _ignored_ by this library, use whitespace between the triple backtick and `clojure`, like this:

    ``` clojure
    ;; ignored by seancorfield/readme
    (do-stuff 42)
    ```

## Caveats

You cannot use `ns` (or `in-ns`) forms in these examples, because the generated tests are all assumed to be in a single namespace (derived from the generated test filename).

Printed output is not considered when running tests. If the REPL session in your readme file needs to show output, it is recommended to show it as comments like this, so that it will be ignored by the generated tests:

```clojure
user=> (println (+ 1 2 3))
;; prints:
; 6
nil
```

## Development

Run the tests:

    $ clojure -A:test:runner

Build a deployable jar of this library:

    $ clojure -A:jar

Install it locally:

    $ clojure -A:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -A:deploy

## License

Copyright © 2020 Sean Corfield, all rights reserved.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
