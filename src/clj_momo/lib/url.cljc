(ns clj-momo.lib.url
  (:require [cemerick.uri :as uri]))

(def url-chars-re #"[-A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%]+")

(def encode uri/uri-encode)

(def decode uri/url-decode)

(defn encoded? [s]
  (boolean
   (re-matches url-chars-re s)))
