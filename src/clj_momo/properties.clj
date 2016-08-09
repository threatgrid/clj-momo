(ns clj-momo.properties
  (:require [clj-momo.lib.map :as map]
            [clj-momo.lib.schema :as mls]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [schema.coerce :as c])
  (:import java.util.Properties))

(defn build-coercer [schema]
  (c/coercer! schema
              c/string-coercion-matcher))

(defn coerce-properties [schema properties-map]
  ((build-coercer schema) properties-map))

(defn read-property-files
  "Read all the property files (that exist) and merge the properties
   into a single map"
  [files]
  (->> files
       (keep (fn [file]
               (when-let [rdr (some-> file io/resource io/reader)]
                 (with-open [rdr rdr]
                   (doto (Properties.)
                     (.load rdr))))))
       concat
       (into {})))

(defn prop->env
  "Convert a property name into an environment variable name"
  [prop]
  (-> prop
      str/upper-case
      (str/replace #"[-.]" "_")))

(defn make-property-env-map
  "Map of environment variable name to property name for the given schema"
  [schema]
  (into {}
        (map (fn [prop]
               [(prop->env prop) prop])
             (mls/keys schema))))

(defn transform
  "Convert a flat map of property->value into a nested map with keyword
   keys, splitting on '.'"
  [properties]
  (reduce (fn [accum [k v]]
            (let [parts (->> (str/split k #"\.")
                             (map keyword))]
              (cond
                (empty? parts) accum
                (= 1 (count parts)) (assoc accum (first parts) v)
                :else (map/rmerge accum
                                  (assoc-in {} parts v)))))
          {}
          properties))

(defn read-env-variables
  "Get a map of properties from environment variables"
  [schema]
  (let [property-env-map (make-property-env-map schema)]
    (into {}
          (map (fn [[env val]]
                 [(get property-env-map env) val])
               (select-keys (System/getenv)
                            (keys property-env-map))))))

(defn build-init-fn
  "Build a function that will read a properties file, merge it with
   system properties, coerce and validate it, transform it into a
   nested map with keyword keys, and then store it in memory."
  [files schema properties-atom]
  (fn init! []
    (->> (merge (read-property-files files)
                (select-keys (System/getProperties)
                             (mls/keys schema))
                (read-env-variables schema))
         (coerce-properties schema)
         transform
         (reset! properties-atom))))
