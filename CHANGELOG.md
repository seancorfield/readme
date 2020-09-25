# Change Log

## Stable Builds

* 2020-09-25 -- 1.0.16
  * Exit with non-zero status if test file cannot be required (syntax errors etc).

* 2020-09-25 -- 1.0.15
  * Fix #4 by calling `make-parents` before spitting the file.

* 2020-03-08 -- 1.0.13
  * Exit with non-zero status if tests produce any failures or errors, and call `shutdown-agents`.
  * Add `:main-opts` to alias in README to make usage easier.

* 2020-03-03 -- 1.0.12
  * Support `user=>` REPL session style as well.
  * Use `run-tests` to get pass/fail summary printed.
  * Improve documentation!

* 2020-03-02 -- 1.0.8
  * Initial version
