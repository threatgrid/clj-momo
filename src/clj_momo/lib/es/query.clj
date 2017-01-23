(ns clj-momo.lib.es.query
  (:require [clojure.string :as str]))

(defn bool
  "Boolean Query"
  [opts]
  {:bool opts})

(defn filtered
  "Filtered query"
  [opts]
  {:filtered opts})

(defn nested
  "Nested document query"
  [opts]
  {:nested opts})

(defn term
  "Term Query"
  ([key values] (term key values nil))
  ([key values opts]
   (merge { (if (coll? values) :terms :term) (hash-map key values) }
          opts)))

(defn terms
  "Terms Query"
  ([key values] (terms key values nil))
  ([key values opts]
   (term key values opts)))

(defn nested-terms [filters]
  "make nested terms from a filter:
  [[[:observable :type] ip] [[:observable :value] 42.42.42.1]]
  ->
  [{:terms {observable.type [ip]}} {:terms {observable.value [42.42.42.1]}}]

we force all values to lowercase, since our indexing does the same for all terms."
  (vec (map (fn [[k v]]
              (terms (->> k
                          (map name)
                          (str/join "."))
                     (map str/lower-case
                          (if (coll? v) v [v]))))
            filters)))

(defn filter-map->terms-query
  "transforms a filter map to en ES terms query"
  ([filter-map]
   (filter-map->terms-query filter-map nil))
  ([filter-map query]

   (let [terms (map (fn [[k v]]
                      (let [t-key (if (sequential? k) k [k])]
                        [t-key v]))
                    filter-map)
         must-filters (nested-terms terms)]

     (cond
       ;; a filter map and a query
       (and filter-map query)
       {:bool
        {:filter (conj must-filters query)}}

       ;; only a filter map
       (and filter-map (nil? query))
       {:bool
        {:filter must-filters}}

       ;; a query without a filter map
       (and (empty? filter-map) query)
       {:bool
        {:filter query}}

       ;; if we neither have a filter map or a query
       :else
       {:bool
        {:match_all {}}}))))
