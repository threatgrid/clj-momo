(ns clj-momo.lib.schema
  (:refer-clojure :exclude [keys]))


(defn keys
  "Get the set of keys from a schema, looking up :k in each key (if its a map)"
  [s]
  (->> (clojure.core/keys s)
       (map #(if (map? %) (:k %) %))
       set))
