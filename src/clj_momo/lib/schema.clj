(ns clj-momo.lib.schema
  (:refer-clojure :exclude [keys]))


(defn keys
  "Get the keys from a schema, looking up :k in each key (if its a map)"
  [s]
  (map #(if (map? %) (:k %) %)
       (clojure.core/keys s)))
