(ns clj-momo.lib.schema-test
  (:require [clj-momo.lib.schema :as sut]
            [clojure.test :refer [deftest is]]
            [schema.core :as s]))

(s/defschema example-schema
  {:foo s/Any
   (s/required-key "bar") s/Any
   (s/optional-key :spam) s/Any
   (s/optional-key "eggs") s/Any})

(deftest test-keys
  (is (= #{:foo "bar" :spam "eggs"}
         (sut/keys example-schema)))
  )
