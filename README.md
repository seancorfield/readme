# seancorfield.readme

A simple library that turns examples in your `README.md` file into tests and runs them!

This project follows the version scheme MAJOR.MINOR.COMMITS where MAJOR and MINOR provide some relative indication of the size of the change, but do not follow semantic versioning. In general, all changes endeavor to be non-breaking (by moving to new names rather than by breaking existing names). COMMITS is an ever-increasing counter of commits since the beginning of this repository.

Latest stable release: 1.0.8

## Usage

Add an alias to your `~/.clojure/deps.edn` file:

```clojure
    :readme {:extra-deps {seancorfield/readme {:mvn/version "1.0.8"}}}
```

Then the most basic usage is:

    clojure -A:readme -m seancorfield.readme

This turns `README.md`'s examples into tests in `src/readme.clj`, loads and runs them, and then deletes that generated file (line numbers in failures should match line numbers in your original `README.md` file).

You can optionally provide a different file path for the readme and for the generated file:

    clojure -A:readme -m seancorfield.readme test/seancorfield/readme_example.md src/generated_test.clj

If your `README.md` file contains code blocks of the form:

    ```clojure
    some expression
    => result
    ```

This will generate tests of the form:

```clojure
(deftest readme-N
  (is (= result (do some expression))))
```

If there is no `=>` then the Clojure code blocks will be added to the generated test namespace as-is with no direct test. This allows setup code to be shown in the `README.md` file, followed by specific tests.

Each `clojure` code block should be a standalone test. If there are multiple expressions in the code block before `=>` they will be wrapped in a `do`. There must be only one expression after `=>`.

Note: you cannot (currently) use `ns` forms in these examples.

## Development

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
