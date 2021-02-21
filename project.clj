(defproject roam-parser "0.0.0"
  :description "A Roam parser written in Clojure"
  :dependencies [[com.atlassian.commonmark/commonmark "0.16.1"]
                 [com.atlassian.commonmark/commonmark-ext-gfm-strikethrough "0.16.1"]
                 [instaparse/instaparse "1.4.10"]]
  :main parser.core)
