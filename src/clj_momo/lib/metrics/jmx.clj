(ns clj-momo.lib.metrics.jmx
  (:require [metrics.reporters.jmx :as jmx]))

(defn start []
  (jmx/start (jmx/reporter {})))
