(ns clj-momo.test-helpers.logging
  (:require
   [clojure.tools.logging :refer [*logger-factory*]]
   [clojure.tools.logging.impl :as impl]))

;; ----- Logging test helpers based on code from the tools.logging test code
;; See https://github.com/clojure/tools.logging/blob/master/src/test/clojure/clojure/tools/test_logging.clj for usage

(defn get-current-thread []
  (Thread/currentThread))

(defn test-factory [enabled-set
                    last-log-entry-atom
                    agent-used-atom
                    all-log-entries-atom]
  (let [main-thread (get-current-thread)]
    (reify impl/LoggerFactory
      (name [_] "test factory")
      (get-logger [_ log-ns]
        (reify impl/Logger
          (enabled? [_ level] (contains? enabled-set level))
          (write! [_ lvl ex msg]
            (let [log-data [(str log-ns) lvl ex msg]]
              (swap! all-log-entries-atom (fnil conj []) log-data)
              (reset! last-log-entry-atom log-data))
            (reset! agent-used-atom
                    (not (identical? main-thread (get-current-thread))))))))))

(defmacro with-test-logging
  [[enabled-level-set last-log-entry-sym agent-used?-sym all-log-entries-sym] & body]
  (let [enabled-level-set (or enabled-level-set #{:trace :debug :info :warn :error :fatal})
        last-log-entry-sym (or last-log-entry-sym 'last-log-entry-sym)
        agent-used?-sym (or agent-used?-sym 'agent-used?-sym)
        all-log-entries-sym (or all-log-entries-sym 'all-log-entries-sym)]
    `(let [~last-log-entry-sym (atom nil)
           ~agent-used?-sym (atom nil)
           ~all-log-entries-sym (atom nil)]
       (binding [*logger-factory* (test-factory
                                    ~enabled-level-set
                                    ~last-log-entry-sym
                                    ~agent-used?-sym
                                    ~all-log-entries-sym)]
         ~@body))))


(defn was-logged? [log-regex all-log-entries-atom]
  (boolean
   (some #(re-find log-regex %)
         (map #(nth % 3) @all-log-entries-atom))))
