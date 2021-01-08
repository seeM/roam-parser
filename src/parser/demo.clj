(ns parser.demo
  (:require [clojure.pprint :refer [pprint]]
            [clojure.test :refer [deftest is]]
            [instaparse.core :as insta]))

(def roam-text "
Basics:

[[link]]
((ref))
{{roam-render}}
$$latex$$
[alias](target)

Recursive:

[[Nested [[Links]]]]
^^**bold highlights**^^
[html roam]([[Aliases]])
[![img](image-as-alias.com)](www.roamresearch.com)
[**bold__italics^^[[highlight]]^^__**](url)
")

(def parser
  (insta/parser (slurp "roam.ebnf")))

(defn join-consec-strings [xs]
  (reduce (fn [x y]
            (let [lst (last x)]
              (if (and (string? lst) (string? y))
                (conj (pop x) (str lst y))
                (conj x y))))
          [] xs))

(defn parse [s & args]
  (insta/transform
    ; TODO: clean up extra nodes, concat strings, etc
    {}
    ; {:symbol str
    ;  :str str
    ;  :space str
    ;  :endline str
    ;  :ref (fn [& children] [:ref (apply str children)])
    ;  ; :target (fn [& children] [:target (join-consec-strings children)])
    ;  ; :doc (fn [& children] [:doc (join-consec-strings children)])
    ;  :link (fn [& children] [:link (join-consec-strings children)])}
    (apply insta/parses parser s args)))

(defn -main []
  (time (pprint (parse "[an alias [[link[[[[ **bold**](url)" :unhide :all))))

(deftest test-parse
  ; (is (= (parse "[alias](url)") ["foo"]))
  ; (is (= (parse "[alias]([[page]])") ["foo"]))
  (is (= (parse "[an alias [[link] **bold**](url)") []))
  ; (is (= (parse "[[link[[deeply[[nested") []))
  ; (is (= (parse "_~~**strike** through~~_\nfoo") ["foo"]))
  ; (is (= (parse "\n\n\n  alkwejr \n ") ["foo"]))
  )
