(ns test-helpers.core
  (:require [clj-momo.properties :as mp]
            [schema.core :as s]))

(def properties (atom nil))

(defn read-test-properties []
  (->> (mp/read-property-files ["default-test.properties"
                                "test.properties"])
       mp/transform
       (mp/coerce-properties {:es {:host s/Str
                                   :port s/Int}})))

(defn fixture-properties [t]
  (try
    (reset! properties (read-test-properties))
    (t)
    (finally (reset! properties nil))))

(defn get-es-config []
  (:es @properties))
