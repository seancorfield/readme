;; copyright (c) 2020 sean corfield, all rights reserved

(ns seancorfield.readme-test
  "Test the readme processor."
  (:require [clojure.test :refer [deftest]]
            [seancorfield.readme :as sut]))

(deftest readme-conversion
  (sut/test-readme "test/seancorfield/readme_example.md" "test/seancorfield/generated_test.clj"))
