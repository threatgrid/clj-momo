(ns clj-momo.lib.id)

(defn make-id [type]
  #?(:clj (str type "-" (java.util.UUID/randomUUID))))
