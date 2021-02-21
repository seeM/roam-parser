(ns parser.core
  (:require [clojure.test :refer [deftest is]]))

(def delimiter-chars
  [\* \_ \^ \[ \( \{])

(defn run-length
  "Returns the length of a run of chr in s from start."
  ([chr s] (run-length chr s 0))
  ([chr s start]
   (->> (subs s start)
        (keep-indexed #(when (not= chr %2) %1))
        first
        (#(or % (count s))))))

(defn delimiter-run
  "Returns a delimiter run of chr in s from start, nil if no run exists.
  A delimiter run is a map with keys :chr :length."
  ([chr s] (delimiter-run chr s 0))
  ([chr s start]
   (let [length (run-length chr s start)]
     (when (> length 0)
       {:chr chr, :length length}))))

; TODO: Need somth like handle-delims
;       It assumes a del exists at pos
;       It counts the length
;       Creates a text node with content of del
;       Creates a del pointing to text node and with length

; (defn delimiter-stack
;   "Returns a stack of delimiter runs."
;   [s]
;   (let [slen (count s)]
;     (loop [n 0
;            stack []]
;       (if (>= n slen)
;         stack
;         (let [chr (nth s n)]
;           (if (= chr \*)
;             (let [length (run-length chr s n)]
;               (when (> length 0)
;                 (recur (+ n length) (conj stack {:chr \*, :length length}))))
;             (recur (inc n) stack)))))))

(defn del-match? [x y]
  (= (:chr x) (:chr y)))

; (defn dels-keep-match-index
;   [dels del]
;   (keep-indexed #(when (del-match? del %2) %1)))

; (defn del-match
;   [dels del]
;   (keep-index #(when (= (:chr %2) (:chr del) %1))))

; TODO: This is O(n)?
(defn last-match
  "Returns the last delimiter run that matches del (has the same :chr),
  nil if there is no match."
  [dels del]
  (->> dels
       (keep-indexed #(when (del-match? del %2) %1))
       last))

; (defn process-delimiters
;   [s pos tree dels chr]
;   ; Scan the delimiter
;   (let [closer (delimiter-run chr s pos)
;         ; Check for a match
;         opener (last-match dels closer)]
;     ; If there isn't a match:
;     ; - add the contents of del to the tree
;     ; - add del to the stack.
;     (if (nil? opener)
;       (let [end (+ pos (:length closer))
;             content (subs s pos end)
;             tree (conj tree [:text content])
;             dels (conj dels closer)]
;         [tree end dels])
;       ; If there is a match:
;       ; - consume from dels and from text nodes (?)
;       (let [use-delims (if (and (>= (:length opener) 2)
;                                 (>= (:length closer) 2))
;                          2 1)
;             opener (assoc opener :length (- (:length opener) use-delims))
;             closer (assoc closer :length (- (:length closer) use-delims))]
;         )
;       ; - make a new node that consumes surrounded nodes (?)
;       )
;         ; 2. Add the contents of the delimiter run to the parse tree
;     (rseq )

;     ; Move forward passed the delimiter run
;     (recur (+ pos (:length delimiter-run) tree dels))))

(def re-main #"^[^*_^`|\[\]!{}$()#]+")

(defn parse-string
  [s pos tree]
  (if-let [match (re-find re-main (subs s pos))]
    ; TODO: Can use matcher end instead of count match?
    (let [end (+ pos (count match))]
      [end (conj tree [:text (subs s pos end)])])
    [pos tree]))

; (defn parse-block
;   [s]
;   (let [length (count s)]
;     (loop [state {:pos 0 :tree [] :dels []}]
;       (if (>= pos length)
;         tree
;         (let [c (nth s pos)]
;           (cond
;             ; (= c \*)
;             :else (apply (parse-string s pos tree))))))))
;             ; :else (recur (inc pos) (conj tree [:text c]) dels)))))))

(defn -main []
  (println (run-length \* "**bold**" 2)))

(deftest test-run-length
  (is (= (run-length \* "**a") 2))
  (is (= (run-length \* "**a" 1) 1))
  (is (= (run-length \* "a") 0))
  (is (= (run-length \* "***") 3))
  (is (= (run-length \_ "_") 1)))

(deftest test-delimiter-run
  (is (= (delimiter-run \* "**a") {:chr \* :length 2}))
  (is (= (delimiter-run \* "**a" 1) {:chr \* :length 1}))
  (is (= (delimiter-run \* "a") nil))
  (is (= (delimiter-run \* "***") {:chr \* :length 3}))
  (is (= (delimiter-run \_ "_") {:chr \_ :length 1})))

(deftest test-last-match
  (is (= (last-match [{:chr \*} {:chr \_} {:chr \*} {:chr \_}] {:chr \*}) 2)))

; (deftest test-delimiter-stack
;   (is (= (delimiter-stack "foo*bar**baz***")
;          [{:chr \*, :length 1} {:chr \*, :length 2} {:chr \*, :length 3}])))

(deftest test-parse-string
  (is (= (parse-string "*foo" 0 []) [0 []]))
  (is (= (parse-string "foo" 0 []) [3 [[:text "foo "]]]))
  (is (= (parse-string "f[oo" 0 []) [1 [[:text "f"]]])))

; (deftest test-parse-block
;   (is (= (parse-block "**foo**")
;          [[:text \*] [:text \*] [:text \f] [:text \o] [:text \o] [:text \*] [:text \*]])))
