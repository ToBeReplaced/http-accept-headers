(ns org.tobereplaced.http-accept-headers
  "Parse HTTP Accept headers in accordance with RFC 2616.

  This library intends to be 100% compliant with RFC 2616. It is
  web-framework agnostic and simply provides a way to go from various
  Accept headers to sequences of their values.

  Parsers are implemented are Accept, Accept-Charset, Accept-Encoding,
  and Accept-Language. In all cases, you may specify what you would
  like to use in place of wildcard values.

  Each parser returns a sequence of values corresponding to the header
  that can be used to provide an appropriate response. The sequence is
  ordered as per RFC 2616 sections 14.1-14.4, honoring the quality
  parameter and all other required comparisons.

  It is assumed that any default values you pass in place of wildcards
  will be acceptible to your server, so they are not expanded into all
  possible combinations.

  This library assumes that the server does not have a preference when
  it receives more than one field with the same quality value. If the
  server would like to have control over this, a different workflow is
  required."
  (:require [clojure.string :refer [split trim join]]))

(defn- trim-and-lowercase
  "Trims the whitespace from the string and lowercases it."
  [s]
  (.toLowerCase (trim s)))

(defn- format-split
  "Splits the string into two based on the regular expression, then
  trims and lowercases each part."
  [s re]
  (mapv trim-and-lowercase (split s re 2)))

(defn- parse-header-element
  "Returns a map of :field, :q, and :params from an Accept* header
  element. Each entry will be trimmed and lowercased."
  [s]
  (let [[field & param-strings] (split s #";")
        params (->> param-strings (map #(format-split % #"=")) (into {}))]
    {:field (trim-and-lowercase field)
     :q (if-some [q (get params "q")] (Double/parseDouble q) 1.0)
     :params (dissoc params "q")}))

(defn- parse-header-elements
  "Returns a sequence of element maps for a Accept* header."
  [header]
  (map parse-header-element (split header #",")))

(defn parse-accept
  "Returns an ordered sequence of vectors containing a media-type and
  a hash-map of its parameters according to the Accept header. The
  wildcards should be a map containing media types for all expected
  media-ranges containing wildcards, like \"*\" or \"*/*\" or
  \"text/*\". The behavior of non-conforming Accept headers is
  undefined and should not be relied on.

  Note that the absence of an Accept header implies that any media
  type is acceptable.

  An example replacements map might be: {\"*\" \"text/html\" \"*/*\"
  \"text/html\" \"text/*\" \"text/html\"}"
  [wildcards accept-header]
  (if (seq accept-header)
    (->> accept-header
         parse-header-elements
         (sort-by (comp count :params) >)
         (sort-by #(let [[content-type sub-type] (format-split (:field %) #"/")]
                     (condp = "*"
                       content-type 2
                       sub-type 1
                       0)))
         (sort-by :q >)
         (take-while (comp pos? :q))
         (map #(let [{:keys [field params]} %]
                 [(get wildcards field field) params])))
    [[(get wildcards "*") {}]]))

(defn accept-wildcards
  "Returns a map suitable for use as the wildcards map with
  parse-accept from media-type strings like \"text/html\". Each
  media-type will add a pairing from type/* to type/subtype. The first
  media-type will also add pairings from * and */*. The media-type
  strings must be lowercase. This is a utility function and is not
  meant to cover all use cases."
  [default & more]
  (let [wildcard #(join "/" [(first (split % #"/" 2)) "*"])]
    (into {"*" default
           "*/*" default
           (wildcard default) default}
          (map #(vector (wildcard %) %) more))))

(defn- qualified-fields
  "Returns an ordered sequence of fields from those elements that have
  positive q values."
  [wildcards elements]
  (->> elements
       (sort-by :q >)
       (take-while (comp pos? :q))
       (map :field)
       (map #(get wildcards % %))))

(defn parse-accept-charset
  "Returns an ordered sequence of charsets according to the
  Accept-Charset header. The default will be used in place of the *
  wildcard and should be lowercase. The behavior of non-conforming
  Accept-Charset headers is undefined and should not be relied on.

  Note that the absence of an Accept-Charset implies that any charset
  is acceptable. The default will be returned in this case."
  [default accept-charset]
  (if (seq accept-charset)
    (let [partial-elements (parse-header-elements accept-charset)
          ;; If there is no * or iso-8859-1 is not explicitly
          ;; mentioned, it gets a q value of 1.  We do this here
          ;; instead of after sorting so we can make sure to place it
          ;; *after* other q = 1.0 values.
          elements (concat partial-elements
                           (when-not (some #{"iso-8859-1" "*"}
                                           (map :field partial-elements))
                             [{:field "iso-8859-1" :q 1.0}]))]
      (qualified-fields {"*" default} elements))
    [default]))

(defn parse-accept-encoding
  "Returns an ordered sequence of content-codings according to the
  Accept-Encoding header. The default will be used in place of the *
  wildcard and should be lowercase. The behavior of non-conforming
  Accept-Encoding headers is undefined and should not be relied on.

  Note that the absence of an Accept-Encoding implies that the only
  acceptable encoding is the identity, which will be returned."
  [default accept-encoding]
  (if (seq accept-encoding)
    (let [elements (parse-header-elements accept-encoding)
          fields (qualified-fields {"*" default} elements)]
      ;; If identity is not specified, it is acceptable with lowest
      ;; priority.
      (concat fields
              (when-not (some #{"identity" "*"} (map :field elements))
                ["identity"])))
    ["identity"]))

(defn parse-accept-language
  "Returns an ordered sequence of language tags according to the
  Accept-Language header. The wildcards should be a map containing
  tags for all non-prefixed language ranges and a tag for the *
  wildcard. Language tags should be lowercase. The behavior of
  non-conforming Accept-Language headers is undefined and should not
  be relied on.

  Note that the absence of an Accept-Language implies that any
  language is acceptable. The language-tag corresponding the the *
  wildcard will be returned.

  An example wildcards map might be: {\"en\" \"en-gb\" \"*\"
  \"en-gb\"}"
  [wildcards accept-language]
  (if (seq accept-language)
    (->> accept-language
         parse-header-elements
         (qualified-fields wildcards))
    [(get wildcards "*")]))

(defn accept-language-wildcards
  "Returns a map suitable for use as the wildcards map with
  parse-accept-language from language-tags like \"en-us\". Each tag
  will add a pairing from its prefix to the tag. The first tag will
  also be used as the value for *. The language tags must be
  lowercase. This is a utility function and is not meant to cover all
  use cases."
  [default & more]
  (let [prefix #(first (split % #"-" 2))]
    (into {"*" default
           (prefix default) default}
          (map #(vector (prefix %) %) more))))
