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
  (is (= {:c1 {:a :b}
          :c2 {:a :b}
          :c3 :c3
          :c4 {:a :b :x :y}
          :d1 :v
          :d2 :v
          :d3 :d3
          :d4 {:x :y}
          :e1 nil
          :e2 nil
          :e3 :e3
          :e4 {:x :y}
          :f2 nil
          :f3 :f3
          :f4 {:x :y}}
         (sut/deep-merge
          {:c1 {:a :b}
           :c2 {:a :b}
           :c3 {:a :b}
           :c4 {:a :b}
           :d1 :v
           :d2 :v
           :d3 :v
           :d4 :v
           :e1 nil
           :e2 nil
           :e3 nil
           :e4 nil}
          {:c2 nil
           :c3 :c3
           :c4 {:x :y}
           :d2 nil
           :d3 :d3
           :d4 {:x :y}
           :e2 nil
           :e3 :e3
           :e4 {:x :y}
           :f2 nil
           :f3 :f3
           :f4 {:x :y}}))))
