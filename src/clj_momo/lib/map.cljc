(ns clj-momo.lib.map
  (:require [clojure.set :as set]))

(defn rmerge
  [& vals]
  (if (every? map? vals)
    (apply merge-with rmerge vals)
    (last vals)))

;; from http://stackoverflow.com/questions/21768802/how-can-i-get-the-nested-keys-of-a-map-in-clojure
(defn keys-in [m]
  (if (map? m)
    (vec
     (mapcat (fn [[k v]]
               (let [sub (keys-in v)
                     nested (map #(into [k] %) (remove empty? sub))]
                 (if (seq nested)
                   nested
                   [[k]])))
             m))
    []))

(defn keys-in-all [& ms]
  (apply set/intersection (->> ms
                               (map keys-in)
                               (map set))))

(defn assoc-if
  "Assoc key/value pairs for some predicate on key-value values

   (assoc-if #(< (count (str %1 %2)) 5) {} :a 1 :b 2)
   => {:a 1 :b 2}
   (assoc-if #(< (count (str %1 %2)) 5) {} :waytoolong 1 :b 2)
   => {:b 2}
  "
  ([p m k v]
   (if (p k v) (assoc m k v) m))
  ([p m k v & more]
   (apply assoc-if p (assoc-if p m k v) more)))

(def assoc-some
  "Assoc key/value pairs for non-nil values


   (assoc-some {} :a 1 :b nil :c 3)
   => {:a 1 :c 3}
  "
  (partial assoc-if #(some? %2)))
