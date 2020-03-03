;; copyright (c) 2020 sean corfield, all rights reserved

(ns seancorfield.readme
  "Turn a README file into a test namespace."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as ct]))

(defn- parse-forms
  "Given a series of Clojure forms, arrange them into pairs of
  `expected` and `actual` for use in tests, followed by an optional
  hash map containing any 'leftover' forms.

  A sequence can begin with `user=>` followed by exactly one `actual`
  form and exactly one `expected` form, or it can begin with any number
  of `actual` forms (that will be grouped with a `do`) followed by `=>`
  and then exactly one `expected` form. Any remaining forms will be
  returned in a map with the key `::do` to be spliced into a `do`."
  [body]
  (loop [pairs [] [prompt actual expected & more :as forms] body]
    (cond (< (count forms) 3)
          (conj pairs {::do forms})

          (= 'user=> prompt)
          (recur (conj pairs [expected actual]) more)

          :else
          (let [actual   (take-while #(not= '=> %) forms)
                expected (when-not (= (count forms) (count actual))
                           (drop (inc (count actual)) forms))]
            (if (seq expected)
              (if (= 1 (count actual))
                (recur (conj pairs [(first expected) (first actual)])
                       (rest expected))
                (recur (conj pairs [(first expected) (cons 'do actual)])
                       (rest expected)))
              (conj pairs {::do actual}))))))

(defmacro defreadme
  "Wrapper for deftest that understands readme examples."
  [name & body]
  (let [organized     (partition-by vector? (parse-forms body))
        [pairs tails] (if (map? (ffirst organized))
                        [(second organized) (first organized)]
                        organized)
        assertions    (map (fn [[e a]] `(ct/is (~'= ~e ~a))) pairs)
        other-forms   (map ::do tails)]
    (if (seq assertions)
      (if (seq other-forms)
        `(do (ct/deftest ~name ~@assertions) ~@(first other-forms))
        `(ct/deftest ~name ~@assertions))
      (if (seq other-forms)
        `(do ~@(first other-forms))
        nil))))

(defn- test-ns
  "Given a file path, return the namespace for it.
  Assumes that the first element will be `src` or `test` so the path is
  relative to the project root."
  [readme-test]
  (-> readme-test
      (str/replace "_" "-")
      (str/split #"[/\\\.]")
      (butlast)
      (rest)
      (->> (str/join "."))))

(defn readme->test
  "Given the path to a README, generate a plain old `clojure.test` file
  at the specified path. If the test path exists, it will be overwritten."
  [readme readme-test]
  (let [in      (io/reader readme)
        ns-test (test-ns readme-test)]
    (loop [[line & lines] (line-seq in)
           copy false line-no 1
           test-lines []]
      (if line
        (cond (str/starts-with? line "```clojure")
              (recur lines true  (inc line-no)
                     (conj test-lines
                           (str "(seancorfield.readme/defreadme readme-" line-no)))
              (and copy (= line "```"))
              (recur lines false (inc line-no)
                     (conj test-lines ")"))
              :else
              (recur lines copy  (inc line-no)
                     (conj test-lines (if copy line ""))))
        (spit readme-test
              (str "(ns " ns-test " (:require [seancorfield.readme]))"
                   (str/join "\n" test-lines)))))))

(defn -main
  "A useful default test behavior that can be invoked from the command
  line via `-m seancorfield.readme`

  This turns `README.md` (if it exists) into `src/readme.clj`, then
  requires it and runs its tests, and finally deletes `src/readme.clj`.

  Optional arguments for the readme file and the generated test can override
  the defaults."
  [& [readme readme-test]]
  (let [readme      (or readme "README.md")
        readme-test (or readme-test "src/readme.clj")
        readme-ns   (symbol (test-ns readme-test))]
    (when (.exists (io/file readme))
      (readme->test readme readme-test)
      (when (try
              (require readme-ns :reload)
              ::run-tests
              (catch Throwable t
                (println "\nFailed to require" readme-ns)
                (println (.getMessage t))
                (some->> t (.getCause) (.getMessage) (println "Caused by"))
                (println readme-test "has not been deleted.")))
        (try
          (ct/run-tests readme-ns)
          (finally
            (.delete (io/file readme-test))))))))
