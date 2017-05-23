(ns clj-momo.lib.clj-time.periodic
  (:require #?(:clj  [clj-time.periodic :as periodic-delegate]
               :cljs [cljs-time.periodic :as periodic-delegate])))

(def periodic-seq periodic-delegate/periodic-seq)
