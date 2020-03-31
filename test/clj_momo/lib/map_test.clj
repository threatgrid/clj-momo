(ns clj-momo.lib.map-test
  (:require [clj-momo.lib.map :as sut]
            [clojure.test :refer [deftest is]]))

(deftest test-assoc-if
  (is {:a 10 :c 3}
      (sut/assoc-if #(< (count (str %1 %2)) 5) {} :a 1 :waytoolong 2 :c 3)))

(deftest test-assoc-some
  (is {:a 10 :c 3}
      (sut/assoc-some {} :a 1 :b nil :c 3)))

(deftest deep-merge-test
  (is (= (sut/deep-merge {:a {:aa "ok"
                              :ab 2}}
                         {:a {:ab 3
                              :ac {:aca 0
                                   :acb "ok"}}}
                         {:a {:ab "ok" :ac {:aca "ok"
                                            :acc "ok"}}})
         {:a {:aa "ok"
              :ab "ok"
              :ac {:aca "ok"
                   :acb "ok"
                   :acc "ok"}}}))
  (is (= {:a :b} (sut/deep-merge nil {:a :b})))
  (is (= {:a :b} (sut/deep-merge {:a :b} nil)))
  (is (nil? (sut/deep-merge nil nil))))
