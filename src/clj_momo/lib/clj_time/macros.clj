(ns clj-momo.lib.clj-time.macros)

(defmacro immigrate
  "Poor person's (ie CLJS programmer's) intern equivalent"
  [ns names]
  `(do
     ~@(for [name names]
         `(def ~name ~(symbol (str ns "/" name))))))
