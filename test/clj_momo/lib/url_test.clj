(ns clj-momo.lib.url-test
  (:require [clojure.test :refer [deftest is]]
            [clj-momo.lib.url :as url]))

(deftest url-test
  (let [url "https://cisco.com/©"]
    (is (= url
           (-> url url/encode url/decode)))
    (is (not (url/encoded? url)))
    (is (url/encoded? (url/encode url)))))
