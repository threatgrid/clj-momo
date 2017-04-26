(ns clj-momo.lib.es.conn
  (:require [clj-http.conn-mgr :refer [make-reusable-conn-manager]]
            [clojure.tools.logging :as log]
            [clj-momo.lib.es.schemas :refer [ESConn]]
            [schema.core :as s])
  (:import [org.apache.http.impl.conn PoolingClientConnectionManager
            PoolingHttpClientConnectionManager]))

(def default-cm-options {:timeout 30000
                         :threads 100
                         :default-per-route 100})

(def default-opts
  {:as :json
   :content-type :json
   :throw-exceptions false})

(defn make-connection-manager []
  (make-reusable-conn-manager default-cm-options))

(s/defn connect :- ESConn
  "instantiate an ES conn from props"
  [{:keys [transport host port clustername]
    :or {transport :http}}]

  {:cm (make-connection-manager)
   :uri (format "http://%s:%s" host port)})

(defn safe-es-read [{:keys [status body]
                     :as res}]
  (case status
    200 body
    201 body
    404 nil
    400 (do (log/warn "ES query parsing error:" res)
            (throw (ex-info (str "ES query failed" (pr-str res))
                            {:type :query-parsing-error
                             :es-http-res res})))
    (do (log/warn "ES query failed:" res)
        (throw (ex-info (str "ES query failed" (pr-str res))
                        {:type :unknown-error
                         :es-http-res res})))))
