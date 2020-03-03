;; copyright (c) 2020 sean corfield, all rights reserved

(ns seancorfield.readme
  "Turn a README file into a test namespace."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as ct]))

(defmacro defreadme
  "Wrapper for deftest that understands readme examples."
  [name & body]
  (let [actual   (take-while #(not= '=> %) body)
        expected (when-not (= (count body) (count actual))
                   (drop (inc (count actual)) body))]
    (if (seq expected)
      (if (= 1 (count expected))
        (if (= 1 (count actual))
          `(ct/deftest ~name (ct/is (~'= ~(first expected) ~(first actual))))
          `(ct/deftest ~name (ct/is (~'= ~(first expected) (do ~@actual)))))
        (if (= 1 (count actual))
          `(ct/deftest ~name (ct/is (~'= (do ~@expected) ~(first actual))))
          `(ct/deftest ~name (ct/is (~'= (do ~@expected) (do ~@actual))))))
      `(do ~@body))))

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
      (try
        (require readme-ns :reload)
        (ct/test-ns readme-ns)
        (finally
          (.delete (io/file readme-test)))))))
