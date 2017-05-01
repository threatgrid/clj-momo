(ns clj-momo.lib.es.document-test
  (:require [clj-momo.test-helpers.core :as mth]
            [clojure.test :refer [deftest is join-fixtures testing use-fixtures]]
            [clj-momo.lib.es
             [conn :as es-conn]
             [document :as es-doc]
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

(deftest ^:integration document-crud-ops
  (testing "with ES conn test setup"
    (let [conn (es-conn/connect
                (th/get-es-config))]

      (es-index/delete! conn "test_index")

      (testing "all ES Document CRUD operations"
        (let [sample-doc {:id "test_doc"
                          :foo "bar is a lie"
                          :test_value 42}
              sample-docs (repeatedly 10 #(hash-map :id (java.util.UUID/randomUUID)
                                                    :_index "test_index"
                                                    :_type "test_mapping"
                                                    :bar "foo"))]
          (is (nil?
               (es-doc/get-doc conn
                               "test_index"
                               "test_mapping"
                               (:id sample-doc))))

          (is (= sample-doc
                 (es-doc/create-doc conn
                                    "test_index"
                                    "test_mapping"
                                    sample-doc
                                    true)))
          (is (= sample-docs
                 (es-doc/bulk-create-doc conn
                                         sample-docs
                                         true)))

          (is (= sample-doc
                 (es-doc/get-doc conn
                                 "test_index"
                                 "test_mapping"
                                 (:id sample-doc))))

          (is (= {:data [sample-doc]
                  :paging {:total-hits 1}}
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
                                  true)))))

      (es-index/delete! conn "test_index"))))
