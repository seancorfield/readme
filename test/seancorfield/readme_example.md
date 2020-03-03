This is an example readme file.

It has some setup:

```clojure
(require '[clojure.string :as str])
```

and an example using `=>`:

```clojure
(str/starts-with? "example" "ex")
=> true
```

and a series of examples using `user=>`:

```clojure
user=> (+ 1 2 3)
;; comments
6
; are
; ignored
user=> (* 1 2 3)
6
user=> (- 1 2 3)
-4
user=> (println "Hello!")
;; prints:
; Hello!
nil
```

and another `=>` example with trailing forms:

```clojure
(str/ends-with? "example" "nope")
=> false
(println "This should print!")
```
