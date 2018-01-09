(ns clj-momo.lib.es.document
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [cemerick.url :refer (url url-encode)]
            [clj-momo.lib.es
             [conn :refer [default-opts
                           safe-es-read
                           safe-es-bulk-read
                           make-default-opts]]
             [schemas :refer [ESConn Refresh]]
             [pagination :as pagination]
             [query :refer [filter-map->terms-query]]]
            [schema.core :as s]))

(def default-limit 1000)

(defn create-doc-uri
  "make an uri for document creation"
  [uri index-name mapping id]
  (str (url uri (url-encode index-name) (url-encode mapping) (url-encode id))))

(def delete-doc-uri
  "make an uri for doc deletion"
  create-doc-uri)

(def get-doc-uri
  "make an uri for doc retrieval"
  create-doc-uri)

(defn update-doc-uri
  "make an uri for document update"
  [uri index-name mapping id]
  (str (url uri (url-encode index-name) (url-encode mapping) (url-encode id) "_update")))

(defn bulk-uri
  "make an uri for bulk action"
  [uri]
  (str (url uri "_bulk")))

(defn search-uri
  "make an uri for search action"
  [uri index-name mapping]
  (str (url uri (url-encode index-name) (url-encode mapping) "_search")))

(def ^:private special-operation-keys
  "all operations fields for a bulk operation"
  [:_doc_as_upsert
   :_index
   :_type
   :_id
   :_retry_on_conflict
   :_routing
   :_percolate
   :_parent
   :_script
   :_script_params
   :_scripted_upsert
   :_timestamp
   :_ttl
   :_version
   :_version_type])

(defn index-operation
  "helper to prepare a bulk insert operation"
  [doc]
  {"index" (select-keys doc special-operation-keys)})

(defn bulk-index
  "generates the content for a bulk insert operation"
  ([documents]
   (let [operations (map index-operation documents)
         documents  (map #(apply dissoc % special-operation-keys) documents)]
     (interleave operations documents))))

(s/defn get-doc
  "get a document on es and return only the source"
  [{:keys [uri cm]} :- ESConn index-name mapping id params]
  (-> (client/get (get-doc-uri uri index-name mapping id)
                  (assoc (make-default-opts params)
                         :connection-manager cm))
      safe-es-read
      :_source))

(s/defn create-doc
  "create a document on es return the created document"
  [{:keys [uri cm]} :- ESConn
   index-name :- s/Str
   mapping :- s/Str
   {:keys [id] :as doc} :- s/Any
   refresh? :- Refresh]
  (safe-es-read
   (client/put (create-doc-uri uri index-name mapping id)
               (merge default-opts
                      {:form-params doc
                       :query-params
                       {:refresh refresh?}
                       :connection-manager cm})))
  doc)

(s/defn bulk-create-doc
  "create multiple documents on ES and return the created documents"
  [{:keys [uri cm]} :- ESConn
   docs :- [s/Any]
   refresh? :- Refresh]

  (let [ops (bulk-index docs)
        json-ops (map #(json/generate-string % {:pretty false}) ops)
        bulk-body (-> json-ops
                      (interleave (repeat "\n"))
                      string/join)]
    (-> (client/post (bulk-uri uri)
                     (merge default-opts
                            {:connection-manager cm
                             :query-params {:refresh refresh?}
                             :body bulk-body}))
        safe-es-read
        safe-es-bulk-read)
    docs))

(s/defn update-doc
  "update a document on es return the updated document"
  [{:keys [uri cm]} :- ESConn
   index-name :- s/Str
   mapping :- s/Str
   id :- s/Str
   doc :- s/Any
   refresh? :- Refresh]

  (safe-es-read
   (client/post (update-doc-uri uri index-name mapping id)
                (merge default-opts
                       {:form-params {:doc doc}
                        :query-params {:refresh refresh?}
                        :connection-manager cm})))
  doc)

(s/defn delete-doc
  "delete a document on es, returns boolean"
  [{:keys [uri cm]} :- ESConn
   index-name :- s/Str
   mapping :- s/Str
   id :- s/Str
   refresh? :- Refresh]
  (-> (client/delete (delete-doc-uri uri index-name mapping id)
                     (merge default-opts
                            {:query-params {:refresh refresh?}
                             :connection-manager cm}))
      safe-es-read
      :result
      (= "deleted")))

(defn sort-params
  [sort_by sort_order]
  (let [sort-fields
        (map (fn [field]
               (let [sp (clojure.string/split field #":")]
                 {(first sp)
                  {:order (or (second sp)
                              sort_order)}}))
             (clojure.string/split (name sort_by) #","))]

    {:sort (into {} sort-fields)}))

(defn params->pagination
  [{:keys [sort_by sort_order offset limit search_after]
    :or {sort_by :id
         sort_order :asc
         offset 0
         limit pagination/default-limit}}]
  (merge
   {}
   (when sort_by
     (sort-params sort_by sort_order))
   (when limit
     {:size limit})
   (when (and offset
              (not search_after))
     {:from offset})
   (when search_after
     {:from 0
      :search_after search_after})))

(defn generate-es-params [query filter-map params]
  (let [query-map (filter-map->terms-query filter-map query)]
    (merge (params->pagination params)
           {:query query-map}
           (select-keys params [:sort :_source]))))

(s/defn search-docs
  "Search for documents on ES using a query string search.  Also applies a filter map, converting
   the values in the filter-map into must match terms."
  [{:keys [uri cm]} :- ESConn
   index-name :- s/Str
   mapping :- s/Str
   query :- s/Any
   filter-map :- s/Any
   params :- s/Any]

  (let [es-params (generate-es-params query filter-map params)
        res (safe-es-read
             (client/post
              (search-uri uri index-name mapping)
              (merge default-opts
                     {:form-params es-params
                      :connection-manager cm})))
        hits (get-in res [:hits :total] 0)
        results (->> res :hits :hits (map :_source))
        sort (-> res :hits :hits last :sort)]
    (log/debug "search-docs:" es-params)
    (pagination/response (or results [])
                         (:from es-params)
                         (:size es-params)
                         sort
                         (:search_after params)
                         hits)))
