(ns clj-momo.lib.es.index-test
  (:require [clj-momo.test-helpers.core :as mth]
            [clojure.test :refer [deftest is join-fixtures testing use-fixtures]]
            [clj-momo.lib.es
             [conn :as es-conn]
             [index :as es-index]
             [document :as es-doc]]
            [test-helpers.core :as th]))

(use-fixtures :once
  mth/fixture-schema-validation
  th/fixture-properties)

(deftest index-uri-test
  (testing "should generate a valid index URI"
    (is (= (es-index/index-uri "http://127.0.0.1" "test")
           "http://127.0.0.1/test"))))

(deftest template-uri-test
  (testing "should generate a valid template URI"
    (is (= (es-index/template-uri "http://127.0.0.1" "test")
           "http://127.0.0.1/_template/test"))))

(deftest rollover-uri-test
  (testing "should generate a valid rollover URI"
    (is (= (es-index/rollover-uri "http://127.0.0.1" "test")
           "http://127.0.0.1/test/_rollover"))
    (is (= (es-index/rollover-uri "http://127.0.0.1" "test" nil true)
           "http://127.0.0.1/test/_rollover?dry_run"))
    (is (= (es-index/rollover-uri "http://127.0.0.1" "test" "test2" true)
           "http://127.0.0.1/test/_rollover/test2?dry_run"))
    (is (= (es-index/rollover-uri "http://127.0.0.1" "test" "test2" false)
           "http://127.0.0.1/test/_rollover/test2"))))

(deftest refresh-uri-test
  (testing "should generat a proper refresh URI"
    (is (= (es-index/refresh-uri "http://127.0.0.1" "test-index")
           "http://127.0.0.1/test-index/_refresh"))
    (is (= (es-index/refresh-uri "http://127.0.0.1" nil)
           "http://127.0.0.1/_refresh"))))

(deftest ^:integration index-crud-ops
  (testing "with ES conn test setup"

    (let [conn (es-conn/connect
                (th/get-es-config))]

      (testing "all ES Index CRUD operations"
        (let [index-create-res
              (es-index/create! conn "test_index"
                                {:settings {:number_of_shards 1
                                            :number_of_replicas 1}})
              index-get-res (es-index/get conn "test_index")
              index-close-res (es-index/close! conn "test_index")
              index-open-res (es-index/open! conn "test_index")
              index-delete-res (es-index/delete! conn "test_index")]

          (es-index/delete! conn "test_index")

          (is (true? (boolean index-create-res)))
          (is (= {:test_index
                  {:aliases {}
                   :mappings {}
                   :settings
                   {:index
                    {:number_of_shards "1"
                     :number_of_replicas "1"
                     :provided_name "test_index"}}}}

                 (update-in index-get-res
                            [:test_index :settings :index]
                            dissoc
                            :creation_date
                            :uuid
                            :version)))
          (is (= {:acknowledged true} index-open-res))
          (is (= {:acknowledged true} index-close-res))
          (is (true? (boolean index-delete-res))))))))

(deftest ^:integration rollover-test
  (let [conn (es-conn/connect (th/get-es-config))]
    (es-index/delete! conn "test_index-*")
    (es-index/create! conn
                      "test_index-1"
                      {:settings {:number_of_shards 1
                                  :number_of_replicas 1}
                       :aliases {:test_alias {}}})
    (testing "rollover should not be applied if conditions are not matched"
      (let [{:keys [rolled_over dry_run new_index]}
            (es-index/rollover! conn "test_alias" {:max_age "1d" :max_docs 3})]
        (is (false? rolled_over))
        (is (false? dry_run))
        (is (false? (es-index/index-exists? conn new_index)))))

    (is (= {:rolled_over false :dry_run true}
           (-> (es-index/rollover! conn
                                   "test_alias"
                                   {:max_age "1d" :max_docs 3}
                                   {}
                                   nil
                                   true)
               (select-keys [:rolled_over :dry_run])))
        "rollover dry_run paramater should be properly applied")

    ;; add 3 documents to trigger max-doc condition
    (es-doc/bulk-create-doc conn
                            (repeat 3 {:_index "test_alias"
                                       :_type "whatever"
                                       :foo :bar})
                            "true")

    (testing "rollover dry_run parameter should be properly applied when condition is met"
      (let [{:keys [rolled_over dry_run old_index new_index]}
            (es-index/rollover! conn
                                "test_alias"
                                {:max_age "1d" :max_docs 3}
                                {}
                                nil
                                true)]
        (is (false? rolled_over))
        (is dry_run)
        (is (= old_index "test_index-1"))
        (is (not= new_index old_index))
        (is (false? (es-index/index-exists? conn new_index)))))

    (is (= "test_index_new"
           (:new_index (es-index/rollover! conn
                                           "test_alias"
                                           {:max_age "1d" :max_docs 3}
                                           {}
                                           "test_index_new"
                                           true)))
        "new_index should be equal to the name passed as parameter")

    (testing "rollover should be properly applied when condition is met and dry run set to false"
      (let [{:keys [rolled_over dry_run old_index new_index]}
            (es-index/rollover! conn
                                "test_alias"
                                {:max_age "1d" :max_docs 3}
                                {:number_of_shards 2
                                 :number_of_replicas 3}
                                nil
                                false)
            {:keys [number_of_shards
                    number_of_replicas]} (get-in (es-index/get conn new_index)
                                                 [(keyword new_index) :settings :index])]
        (is rolled_over)
        (is (false? dry_run))
        (is (= old_index "test_index-1"))
        (is (not= old_index new_index))
        (is (= "2" number_of_shards))
        (is (= "3" number_of_replicas))))

    (es-index/delete! conn "test_index-*")))
