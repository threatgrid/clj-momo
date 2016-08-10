(ns clj-momo.test-helpers.core
  (:require [clj-momo.lib.schema :as mls]
            [clojure
             [data :as cd]
             [test :as ct]]
            [schema.core :as schema]))

;;;; Assert Expression Methods
;; e.g. (is (deep= ...))

(defmethod ct/assert-expr 'deep= [msg [_ a b :as form]]
  `(let [[only-a# only-b# unused#] (cd/diff ~a ~b)]
     (if (or only-a# only-b#)
       (let [only-msg# (str (when only-a# (str "Only in A: " only-a#))
                            (when (and only-a# only-b#) ", ")
                            (when only-b# (str "Only in B: " only-b#)))]
         (ct/do-report {:type :fail, :message ~msg,
                        :expected '~form, :actual only-msg#}))
       (ct/do-report {:type :pass, :message ~msg,
                      :expected '~form, :actual nil}))))


;;;; Higher Order / Helper Helpers

(def get-valid-properties
  (memoize mls/keys))

(def build-valid-property?-fn
  (memoize
   (fn memoized-build-valid-property?-fn [properties-schema]
     (let [valid-properties (get-valid-properties properties-schema)]
       (fn valid-property? [property]
         (some #{property} valid-properties))))))

(defn build-set-property-fn [properties-schema]
  (let [valid-property? (build-valid-property?-fn properties-schema)]
    (fn set-property [prop val]
      (assert (valid-property? prop)
              (str "Tried to set unknown property '" prop "'"))
      (System/setProperty prop
                          (if (keyword? val)
                            (name val)
                            (str val))))))

(defn build-clear-property-fn [properties-schema]
  (let [valid-property? (build-valid-property?-fn properties-schema)]
    (fn clear-property [prop]
      (assert (valid-property? prop)
              (str "Tried to clear unknown property '" prop "'"))
      (System/clearProperty prop))))

(defn build-with-properties-map-fn [properties-schema]
  (let [set-property (build-set-property-fn properties-schema)
        clear-property (build-clear-property-fn properties-schema)]
    (fn with-properties-map [properties-map f]
      (doseq [[property value] properties-map]
        (set-property property value))
      (f)
      (doseq [property (keys properties-map)]
        (clear-property property)))))

(defn build-with-properties-vec-fn [properties-schema]
  (let [with-properties-map (build-with-properties-map-fn properties-schema)]
    (fn with-properties-vec [properties-vec f]
      (with-properties-map (apply hash-map properties-vec) f))))

(defn build-fixture-property-fn [properties-schema]
  (let [set-property (build-set-property-fn properties-schema)]
    (fn fixture-property [property value]
      (fn [test]
        (set-property property value)
        (test)))))


;;;; Helpers

(defn clear-properties
  "Remove system properties that match the schema keys.  Presumably
   they would have been set on a prior test run"
  [properties-schema]
  (let [clear-property (build-clear-property-fn properties-schema)]
    (run! clear-property
          (get-valid-properties properties-schema))))

(defn fixture-schema-validation [f]
  (schema/with-fn-validation
    (f)))
