(ns clj-momo.lib.es.query-test
  (:require [clj-momo.lib.es.query :as q]
            [clojure.test :refer :all]))

(deftest filter-map->terms-query-test
  (let [all-of {:a 1 :b [2 3]}
        one-of {[:c :d] 4 :e "a string"}
        query {:match {:title "this is a test"}}

        all-of-filters [{:terms {"a" '("1")}}
                        {:terms {"b" '("2" "3")}}]
        should [{:terms {"c.d" '("4")}}
                {:terms {"e" '("a string")}}]

        bool-query1 (q/filter-map->terms-query all-of query one-of)
        bool-query2 (q/filter-map->terms-query nil nil one-of)
        bool-query3 (q/filter-map->terms-query all-of)
        ]

    (testing "filter-map->terms-query with with all params should return a bool quer with all-of elements and query in filter clause, and one-of elements in should clause"
      (is (= (conj all-of-filters query)
             (get-in bool-query1 [:bool :filter])))
      (is (= should (get-in bool-query1 [:bool :should]))))

    (testing "filter-map->terms-query with only one-of param must be a bool query witn only :should filter"
      (is (nil? (get-in bool-query2 [:bool :filter])))
      (is (= should (get-in bool-query2 [:bool :should]))))

    (testing "filter-map->terms-query with only all-of param mst be a bool query with only filter field with all-of terms"
      (is (= all-of-filters (get-in bool-query3 [:bool :filter])))
      (is (nil? (get-in bool-query3 [:bool :should]))))

    (is (= query
           (q/filter-map->terms-query nil query nil)
           (q/filter-map->terms-query [] query '()))
        "calling with query but empty all-of and one-of params must return query")

    (is (= {:match_all {}}
           (q/filter-map->terms-query nil nil nil)
           (q/filter-map->terms-query [] {} '()))
        "calling with every empty params must return a match-all query")

    ))
