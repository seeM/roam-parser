(ns parser.demo
  (:require [clojure.test :refer [deftest is]]
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
  (insta/parser "
     doc = inline+

     (* inlines *)
     <inline>        = str | space | symbol | endline | symbol-inline
     <symbol-inline> = image | alias | bold | italic | highlight | code | link | ref | roam-render | latex

     (* text *)
     str     = normal-char+ !normal-char
     space   = space-char+ !space-char
     symbol  = !symbol-inline special-char
     (* NOTE: could probably treat endline as space, because no Roam syntax depends on it *)
     endline = newline

     (* alias *)
     alias           = alias-content
     image           = <'!'> alias-content
     <alias-content> = label target
     label           = <'['> (!']' inline)* <']'>
     target          = <'('> (link | !link ( !'(' !')' nonspace-char)+ ) <')'>

     (* styles *)
     bold         = <'**'> !whitespace (!'**' inline)+ <'**'>
     italic       = <'__'> !whitespace (!'__' inline)+ <'__'>
     highlight    = <'^^'> !whitespace (!'^^' inline)+ <'^^'>
     code         =  <'`'> !whitespace (!'`' any-char)+ <'`'>
     <whitespace> = space-char | newline

     (* other *)
     link        = <'[['> ( (!link !']]' any-char)+ | link )+ <']]'>
     ref         = <'(('> (!'))' alphanumeric)+ <'))'>
     roam-render = <'{{'> !whitespace ( (!'{{' any-char)+ | roam-render)+ <'}}'>
     latex       =  <'$$'> !whitespace (!'$$' any-char)+ <'$$'>

     (* characters and tokens *)
     <alphanumeric>  = #'[a-zA-Z0-9]'
     <normal-char>   = !(special-char | space-char | newline) any-char
     <special-char>  = '**' | '__' | '^^' | '`' | '[' | ']' | '!' | '{{' | '}}' | '$$' | '((' | '))'
     <space-char>    = ' ' | '\\t'
     <nonspace-char> = !space-char !newline any-char
     <newline>       = '\\n' | '\\r' '\\n'?
     <any-char>      = #'.'
     <sp>            = space-char*
     <spnl>          = sp (newline sp)?
     "))

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

(deftest test-parse
  ; (is (= (parse "[alias](url)") ["foo"]))
  ; (is (= (parse "[alias]([[page]])") ["foo"]))
  (is (= (parse roam-text) []))
  ; (is (= (parse "_~~**strike** through~~_\nfoo") ["foo"]))
  ; (is (= (parse "\n\n\n  alkwejr \n ") ["foo"]))
  )
