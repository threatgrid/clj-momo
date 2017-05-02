(ns clj-momo.lib.metrics.console
  (:require [metrics.reporters.console :as console]))

(defn start [interval]
  (console/start (console/reporter {}) interval))
