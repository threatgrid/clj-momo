(ns clj-momo.lib.map-test
  (:require [clj-momo.lib.map :as sut]
            [clojure.test :refer [deftest is are testing]]))

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
  (is (nil? (sut/deep-merge nil nil)))
  (is (= {:a {:b :c}}
         (sut/deep-merge {:a {:b :c}} {:a nil})))
  (is (= {:a {:c :d}}
         (sut/deep-merge {:a :b} {:a {:c :d}})))
  (is (= {:a :b}
         (sut/deep-merge {:a {:c :d}} {:a :b})))
  (is (= {:a {:b {:x :y}}}
         (sut/deep-merge {:a {:b :c}}
                         nil
                         {:a nil}
                         {:a {:b {:x :y}}})))
  (is (= {:a :b} (sut/deep-merge {:a :b})))
  (testing "m1 is a hashmap and m2 is different"
    (are [x m2] (= x
                   (sut/deep-merge {:a :b}  m2))
      {:a :b} nil
      :c      :c
      {:a :b
       :c :d} {:c :d}))
  (testing "m2 is a hashmap and m1 is different"
    (are [x m1] (= x
                   (sut/deep-merge m1 {:a :b}))
      {:a :b} nil
      {:a :b} :c
      {:a :b
       :c :d} {:c :d})))
