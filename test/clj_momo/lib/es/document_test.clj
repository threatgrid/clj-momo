(ns clj-momo.lib.es.document-test
  (:require [clj-momo.test-helpers.core :as mth]
            [clojure.test :refer [deftest is join-fixtures testing use-fixtures]]
            [clj-momo.lib.es
             [conn :as es-conn]
             [document :as es-doc]
             [query :as query]
             [index :as es-index]]
            [test-helpers.core :as th]))

(use-fixtures :once
  mth/fixture-schema-validation
  th/fixture-properties)

(deftest create-doc-uri-test
  (testing "should generate a valid doc URI"
    (is (= (es-doc/create-doc-uri "http://127.0.0.1"
                                  "test_index"
                                  "test_mapping"
                                  "test")
           "http://127.0.0.1/test_index/test_mapping/test"))
    (is (= (es-doc/create-doc-uri "http://127.0.0.1"
                                  "test_index"
                                  "test_mapping"
                                  "test/foo/bar")
           "http://127.0.0.1/test_index/test_mapping/test%2Ffoo%2Fbar"))))

(deftest update-doc-uri-test
  (is (= (es-doc/update-doc-uri "http://127.0.0.1"
                                "test_index"
                                "test_mapping"
                                "test"
                                42)
         "http://127.0.0.1/test_index/test_mapping/test/_update?retry_on_conflict=42"))  )

(deftest ^:integration document-crud-ops
  (testing "with ES conn test setup"
    (let [conn (es-conn/connect
                (th/get-es-config))]

      (es-index/delete! conn "test_index")

      (testing "all ES Document CRUD operations"
        (let [sample-doc {:id "test_doc"
                          :foo "bar is a lie"
                          :test_value 42}
              sample-docs
              (repeatedly 10 #(hash-map :id (.toString (java.util.UUID/randomUUID))
                                        :_index "test_index"
                                        :_type "test_mapping"
                                        :bar "foo"))]
          (is (nil?
               (es-doc/get-doc conn
                               "test_index"
                               "test_mapping"
                               (:id sample-doc)
                               {})))

          (is (= sample-doc
                 (es-doc/create-doc conn
                                    "test_index"
                                    "test_mapping"
                                    sample-doc
                                    "true")))

          (let [updated-doc (assoc sample-doc :test_value 43)]
            (is (= updated-doc
                   (es-doc/update-doc conn
                                      "test_index"
                                      "test_mapping"
                                      (:id updated-doc)
                                      updated-doc
                                      "true"
                                      {:retry-on-conflict 10})))

            (let [second-update (assoc sample-doc :test_value 42)]
              (is (= second-update
                     (es-doc/update-doc conn
                                        "test_index"
                                        "test_mapping"
                                        (:id updated-doc)
                                        second-update
                                        "true"))))
            (is (= sample-docs
                   (es-doc/bulk-create-doc conn
                                           sample-docs
                                           "true"))))

          (is (= sample-docs
                 (es-doc/bulk-create-doc conn
                                         sample-docs
                                         "true")))
          (testing "bulk-create-doc with partioning"
            (let [sample-docs-2 (map #(assoc % :test_value 43) sample-docs)]
              (is (= sample-docs-2
                     (es-doc/bulk-create-doc conn
                                             sample-docs-2
                                             "true"
                                             0)))

              (is (= 10
                     (get-in (es-doc/search-docs conn
                                                 "test_index"
                                                 "test_mapping"
                                                 {:query_string {:query "*"}}
                                                 {:test_value 43}
                                                 {:sort_by "test_value"
                                                  :sort_order :desc})
                             [:paging :total-hits])))))


          (is (= sample-doc
                 (es-doc/get-doc conn
                                 "test_index"
                                 "test_mapping"
                                 (:id sample-doc)
                                 {})))

          (is (= {:foo "bar is a lie"}
                 (es-doc/get-doc conn
                                 "test_index"
                                 "test_mapping"
                                 (:id sample-doc)
                                 {:_source ["foo"]})))

          (is (= {:data [sample-doc]
                  :paging {:total-hits 1
                           :sort [42]}}
                 (es-doc/search-docs conn
                                     "test_index"
                                     "test_mapping"
                                     {:query_string {:query "bar"}}
                                     {:test_value 42}
                                     {:sort_by "test_value"
                                      :sort_order :desc})))

          (is (true?
               (es-doc/delete-doc conn
                                  "test_index"
                                  "test_mapping"
                                  (:id sample-doc)
                                  "true")))))

      (es-index/delete! conn "test_index"))))

(deftest partition-json-ops-test
  (is (= [["ops1"] ["ops2"] ["ops3--"]]
         (es-doc/partition-json-ops
          ["ops1" "ops2" "ops3--"]
          1))
      "All elements are in a group if the max size is exceeded")
  (is (= [["ops1" "ops2"] ["ops3--"]]
         (es-doc/partition-json-ops
          ["ops1" "ops2" "ops3--"]
          8))
      "The max size is used to partition ops")
  (is (= [["ops1" "ops2" "ops3--"]]
         (es-doc/partition-json-ops
          ["ops1" "ops2" "ops3--"]
          1000))
      "All ops are in the same group"))

(deftest ^:integration search_after-consistency-test
  (let [docs
        (let [id (.toString (java.util.UUID/randomUUID))]
          (map
           #(hash-map :id id
                      :foo %
                      :test "ok")
           (range 1000)))
        conn (es-conn/connect
              (th/get-es-config))
        search-query #(get-in (es-doc/search-docs conn
                                           "test_index"
                                           "test_mapping"
                                           nil
                                           {}
                                           {:limit 100})
                       [:paging :sort])]
    (es-index/delete! conn "test_index")
    (es-index/create! conn "test_index" {})
    (doseq [doc docs]
      (es-doc/create-doc conn
                         "test_index"
                         "test_mapping"
                         doc
                         "true"))
    (is (apply = (repeatedly 30 search-query)))
    (es-index/delete! conn "test_index")))

(deftest ^:integration count-test
  (let [sample-docs (mapv #(assoc {:_index "test_index"
                                   :_type "test_mapping"
                                   :foo :bar}
                                  :_id %)
                          (range 10))
        conn (es-conn/connect (th/get-es-config))]
    (es-index/delete! conn "test_index")
    (es-index/create! conn "test_index" {})
    (es-doc/bulk-create-doc conn sample-docs "true")
    (is (= 10
           (es-doc/count-docs conn "test_index" "test_mapping")
           (es-doc/count-docs conn "test_index" nil)
           (es-doc/count-docs conn "test_index" "test_mapping" {:term {:foo :bar}})
           (es-doc/count-docs conn "test_index" "test_mapping" {:match_all {}})))
    (is (= 3 (es-doc/count-docs conn "test_index" "test_mapping" {:ids {:values (range 3)}})))
    (es-index/delete! conn "test_index")))

(deftest ^:integration query-test
  (let [sample-docs (mapv #(assoc {:_index "test_index"
                                   :_type "test_mapping"
                                   :foo :bar}
                                  :_id (str %))
                          (range 10))
        conn (es-conn/connect (th/get-es-config))
        sample-3-docs (->> (shuffle sample-docs)
                           (take 3))
        sample-3-ids (map :_id sample-3-docs)
        _ (es-index/delete! conn "test_index")
        _ (es-index/create! conn "test_index" {})
        _ (es-doc/bulk-create-doc conn sample-docs "true")
        ids-query-result-1 (es-doc/query conn
                                         "test_index"
                                         "test_mapping"
                                         (query/ids sample-3-ids)
                                         {})
        ids-query-result-2 (es-doc/query conn
                                         "test_index"
                                         "test_mapping"
                                         (query/ids sample-3-ids)
                                         {}
                                         true)]
    (is (= (repeat 3 {:foo "bar"})
           (:data ids-query-result-1))
        "querying with ids query without full-hits? param should return only source of selected docs in :data")

    (testing "when full-hits is set as true, each element of :data field should contains :_id :_source and :_index fields"
      (is (= (set sample-3-ids)
             (->> (:data ids-query-result-2)
                  (map :_id)
                  set)))
      (is (= (repeat 3 {:foo "bar"})
             (->> (:data ids-query-result-2)
                  (map :_source))))
      (is (= (repeat 3 "test_index")
             (->> (:data ids-query-result-2)
                  (map :_index)))))
    ;; clean
    (es-index/delete! conn "test_index")))
