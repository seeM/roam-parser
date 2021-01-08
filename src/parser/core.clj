(ns parser.core
  (:require [parser.strikethrough :refer [strikethrough-extension]]
            [parser.ref :refer [ref-extension]]
            [parser.link :refer [link-extension]])
  (:import
    (org.commonmark.parser Parser)
    (org.commonmark.renderer.html HtmlRenderer)
    (org.commonmark.renderer.text TextContentRenderer)))

(def extensions [
                 (strikethrough-extension)
                 (ref-extension)
                 (link-extension)
                 ])

(def parser
  (-> (Parser/builder)
      ; Disable all block types
      (.enabledBlockTypes #{})
      (.extensions extensions)
      (.build)))

(def renderer
  (-> (HtmlRenderer/builder)
  ; (-> (TextContentRenderer/builder)
      (.extensions extensions)
      (.build)))

(defn -main [input]
  (->> (slurp input)
       (.parse parser)
       (.render renderer)
       (println)))
