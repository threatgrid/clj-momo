(ns clj-momo.test-helpers.logging
  (:require
   [clojure.tools.logging :refer [*logger-factory*]]
   [clojure.tools.logging.impl :as impl]))

;; ----- Logging test helpers coming from the tools.logging test code
;; See https://github.com/clojure/tools.logging/blob/master/src/test/clojure/clojure/tools/test_logging.clj for usage

(defn test-factory [enabled-set entries-atom agent-used-atom]
  (let [main-thread (Thread/currentThread)]
    (reify impl/LoggerFactory
      (name [_] "test factory")
      (get-logger [_ log-ns]
        (reify impl/Logger
          (enabled? [_ level] (contains? enabled-set level))
          (write! [_ lvl ex msg]
            (reset! entries-atom [(str log-ns) lvl ex msg])
            (reset! agent-used-atom (not (identical? main-thread (Thread/currentThread))))))))))

(defmacro with-test-logging
  [[enabled-level-set log-entry-sym agent-used?-sym] & body]
  (let [enabled-level-set (or enabled-level-set #{:trace :debug :info :warn :error :fatal})
        log-entry-sym (or log-entry-sym 'log-entry-sym)
        agent-used?-sym (or agent-used?-sym 'agent-used?-sym)]
    `(let [~log-entry-sym (atom nil)
           ~agent-used?-sym (atom nil)]
       (binding [*logger-factory* (test-factory
                                    ~enabled-level-set
                                    ~log-entry-sym
                                    ~agent-used?-sym)]
         ~@body))))
