(ns clj-momo.properties-test
  (:require [clj-momo.properties :as sut]
            [clojure.test :refer [deftest is]]
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema example-schema
  {:string s/Str
   :integer s/Int
   :boolean s/Bool
   :keyword s/Keyword})

(deftest test-build-coercer
  (is (= {:string "foo"
          :integer 1
          :boolean true
          :keyword :bar}
         ((sut/build-coercer example-schema)
          {:string "foo"
           :integer "1"
           :boolean "true"
           :keyword "bar"}))))

(deftest test-coerce-properties
  (is (= {:string "foo"
          :integer 1
          :boolean true
          :keyword :bar}
         (sut/coerce-properties example-schema
                                {:string "foo"
                                 :integer "1"
                                 :boolean "true"
                                 :keyword "bar"}))))

(def files ["default-properties-test.properties"
            "properties-test.properties"])

(deftest test-read-property-files
  (is (= {"test.feature1.thing1" "bar"
          "test.feature1.number" "400"
          "test.feature2.thing1" "spam"
          "test.feature2.thing2" "spamity spam"
          "test.feature2.bool" "true"
          "test.feature3.thing1" "optional feature3"}
         (sut/read-property-files files))))

(deftest test-prop-env
  (is (= "TEST_FEATURE_THING"
         (sut/prop->env "test.feature.thing")))
  (is (= "TEST_FEATURE_THING_TWO"
         (sut/prop->env "test.feature.thing_two")))
  (is (= "TEST_FEATURE_THING_WITH_HYPHENS"
         (sut/prop->env "test.feature.thing-with-hyphens"))))

(s/defschema TestPropertiesSchema
  (st/merge
   (st/required-keys {"test.feature1.thing1" s/Str
                      "test.feature1.number" s/Int
                      "test.feature2.thing1" s/Keyword
                      "test.feature2.thing2" s/Str
                      "test.feature2.bool"   s/Bool})
   (st/optional-keys {"test.feature3.thing1" s/Str})))

(deftest test-make-property-env-map
  (is (= {"TEST_FEATURE1_THING1" "test.feature1.thing1"
          "TEST_FEATURE1_NUMBER" "test.feature1.number"
          "TEST_FEATURE2_THING1" "test.feature2.thing1"
          "TEST_FEATURE2_THING2" "test.feature2.thing2"
          "TEST_FEATURE2_BOOL"   "test.feature2.bool"
          "TEST_FEATURE3_THING1" "test.feature3.thing1"}
         (sut/make-property-env-map TestPropertiesSchema))))

(deftest test-transform
  (is (= {:test
          {:feature1 {:thing1 "bar",
                      :number "400"},
           :feature2 {:thing1 "spam",
                      :thing2 "spamity spam",
                      :bool "true"},
           :feature3 {:thing1 "optional feature3"}}}
         (sut/transform
          {"test.feature1.thing1" "bar"
           "test.feature1.number" "400"
           "test.feature2.thing1" "spam"
           "test.feature2.thing2" "spamity spam"
           "test.feature2.bool" "true"
           "test.feature3.thing1" "optional feature3"}))))

(deftest test-build-init-fn
  (let [properties (atom {})
        init! (sut/build-init-fn files
                                 TestPropertiesSchema
                                 properties)]
    (init!)
    (is (= {:test
            {:feature1 {:thing1 "bar",
                        :number 400},
             :feature2 {:thing1 :spam,
                        :thing2 "spamity spam",
                        :bool true},
             :feature3 {:thing1 "optional feature3"}}}
           @properties))))
