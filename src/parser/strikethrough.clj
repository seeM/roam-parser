; Port of org.commonmark.ext.gfm.strikethrough
(ns parser.strikethrough
  (:import
    (org.commonmark.node CustomNode Delimited Nodes SourceSpans)
    (org.commonmark.parser Parser$Builder Parser$ParserExtension)
    (org.commonmark.parser.delimiter DelimiterProcessor)
    (org.commonmark.renderer NodeRenderer)
    (org.commonmark.renderer.text TextContentRenderer$Builder
                                  TextContentRenderer$TextContentRendererExtension
                                  TextContentNodeRendererFactory)
    (org.commonmark.renderer.html HtmlRenderer$Builder
                                  HtmlRenderer$HtmlRendererExtension
                                  HtmlNodeRendererFactory)))

(defn new-strikethrough []
  (proxy [CustomNode Delimited] []
    (getOpeningDelimiter [] "~~")
    (getClosingDelimiter [] "~~")))

(defonce strikethrough-class (class (new-strikethrough)))

(defn strikethrough-delimiter-processor []
  (reify
    DelimiterProcessor
    (getOpeningCharacter [this] \~)
    (getClosingCharacter [this] \~)
    (getMinLength [this] 2)
    ; If successful, mutates nodes after the opening node in openingRun.
    (process [this openingRun closingRun]
      (if (and (>= (.length openingRun) 2) (>= (.length closingRun) 2))
        ; Use exactly two delimiters even if we have more, and don't care about internal openers/closers.

        (let [opener (.getOpener openingRun)
              strikethrough (new-strikethrough)
              sourceSpans (SourceSpans.)]

          ; Wrap nodes between delimiters in strikethrough.
          (.addAllFrom sourceSpans (.getOpeners openingRun 2))
          (doseq [node (Nodes/between opener (.getCloser closingRun))]
            (.appendChild strikethrough node)
            (.addAll sourceSpans (.getSourceSpans node)))
          (.addAllFrom sourceSpans (.getClosers closingRun 2))
          (.setSourceSpans strikethrough (.getSourceSpans sourceSpans))

          ; Mutate node tree.
          (.insertAfter opener strikethrough)

          2)
        0))))

(def strikethrough-node-types #{strikethrough-class})

(defn render-children [node context]
  (loop [x (.getFirstChild node)]
    (when (not (nil? x))
      (do (.render context x)
          (recur (.getNext x))))))

(defn strikethrough-html-node-renderer [context]
  (let [html (.getWriter context)]
    (reify
      NodeRenderer
      (getNodeTypes [this] strikethrough-node-types)
      (render [this node]
        (.tag html "del" (.extendAttributes context node "del" {}))
        (render-children node context)
        (.tag html "/del")))))

(defn strikethrough-text-content-node-renderer [context]
  (let [textContent (.getWriter context)]
    (reify
      NodeRenderer
      (getNodeTypes [this] strikethrough-node-types)
      (render [this node]
        (.write textContent \/)
        (render-children node context)
        (.write textContent \/)))))

(defn strikethrough-html-node-renderer-factory []
  (reify HtmlNodeRendererFactory
    (create [this context] (strikethrough-html-node-renderer context))))

(defn strikethrough-text-content-node-renderer-factory []
  (reify TextContentNodeRendererFactory
    (create [this context] (strikethrough-text-content-node-renderer context))))

(defn strikethrough-extension []
  (reify
    Parser$ParserExtension
    (^void extend [this ^Parser$Builder parserBuilder]
      (.customDelimiterProcessor parserBuilder (strikethrough-delimiter-processor)))

    HtmlRenderer$HtmlRendererExtension
    (^void extend [this ^HtmlRenderer$Builder rendererBuilder]
      (.nodeRendererFactory rendererBuilder (strikethrough-html-node-renderer-factory)))

    TextContentRenderer$TextContentRendererExtension
    (^void extend [this ^TextContentRenderer$Builder rendererBuilder]
      (.nodeRendererFactory rendererBuilder (strikethrough-text-content-node-renderer-factory)))))
