(ns clj-momo.lib.map-test
  (:require [clj-momo.lib.map :as sut]
            [clojure.test :refer [deftest is]]))

(deftest test-rmerge
  (is false))

(deftest test-keys-in
  (is false))

(deftest test-keys-in-all
  (is false))

(deftest test-assoc-if
  (is {:a 10 :c 3}
      (sut/assoc-if #(< (count (str %1 %2)) 5) {} :a 1 :waytoolong 2 :c 3)))

(deftest test-assoc-some
  (is {:a 10 :c 3}
      (sut/assoc-some {} :a 1 :b nil :c 3)))
