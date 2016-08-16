(ns clj-momo.test-helpers.http-assert-2
  (:refer-clojure :exclude [get])
  (:require [clj-momo.test-helpers.core]  ;; Make sure deep= is defined
            [clj-momo.test-helpers.http :refer [delete get post put]]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer [is testing]]
            [clojure.tools.logging :as log]))

(defn test-post
  "Test a successful POST returns the parsed-body.
  An expected result can be provided."
  ([path port new-entity]
   (testing (str "POST " path)
     (let [{status :status
            result :parsed-body
            :as response} (try (post path
                                     port
                                     :body new-entity)
                               (catch Exception e
                                 (pprint e)
                                 {:status 500
                                  :body ""
                                  :parsed-body nil}))]
       (when (not= 200 status)
         (throw (ex-info (str "POST " path
                              "\nExpected status to be 200 but was " status
                              " for " path ":\n"
                              (with-out-str (pprint response)))
                         {:path path
                          :port port
                          :new-entity new-entity
                          :response response})))
       result)))
  ([path port new-entity expected]
   (test-post path port identity new-entity expected))
  ([path port transform-result-fn new-entity expected]
   (testing (str "POST " path)
     (let [{status :status
            result :parsed-body
            :as response} (post path
                                port
                                :body new-entity)]
       (when (not= 200 status)
         (throw (ex-info (str "POST " path
                              "\nExpected status to be 200 but was " status
                              " for " path ":\n"
                              (with-out-str (pprint response)))
                         {:path path
                          :port port
                          :new-entity new-entity
                          :response response})))
       (is (= expected
              (transform-result-fn result)))
       result))))

(defn test-put
  "Test a successful PUT returns the parsed-body.
  An expected result can be provided."
  ([path port new-entity]
   (testing (str "PUT " path)
     (let [{status :status
            result :parsed-body
            :as response} (try (put path
                                    port
                                    :body new-entity)
                               (catch Exception e
                                 (pprint e)
                                 {:status 500
                                  :body ""
                                  :parsed-body nil}))]
       (when (not= 200 status)
         (throw (ex-info (str "PUT " path
                              "\nExpected status to be 200 but was " status
                              " for " path ":\n"
                              (with-out-str (pprint response)))
                         {:path path
                          :port port
                          :new-entity new-entity
                          :response response})))
       result)))
  ([path port new-entity expected]
   (testing (str "PUT " path)
     (let [{status :status
            result :parsed-body
            :as response} (put path
                               port
                               :body new-entity)]
       (when (not= 200 status)
         (throw (ex-info (str "PUT " path
                              "\nExpected status to be 200 but was " status
                              " for " path ":\n"
                              (with-out-str (pprint response)))
                         {:path path
                          :port port
                          :new-entity new-entity
                          :response response})))
       (is (= expected
              result))
       result))))

(defn test-get
  "Helper which test a get request occurs with success and return the right object
  Returns the result of the GET call."
  ([path port expected-entity]
   (test-get path port identity expected-entity))
  ([path port transform-result-fn expected-entity]
   (testing (str "GET " path)
     (let [resp (get path
                     port)]
       (when (get-in resp [:parsed-body :message])
         (log/error (get-in resp [:parsed-body :message])))
       (when (get-in resp [:parsed-body :errors])
         (log/error (get-in resp [:parsed-body :errors])))
       (is (= 200 (:status resp)))
       (when (= 200 (:status resp))
         (is (= expected-entity
                (transform-result-fn (:parsed-body resp))))
         (:parsed-body resp))))))

(defn test-delete
  "Helper which test a delete request occurs with success"
  [path port]
  (testing (str "DELETE " path)
    (let [resp (delete path
                       port)]
      (is (= 204 (:status resp)))
      (= 204 (:status resp)))))

(defn with-port-fn
  "Helper to compose a fn that knows how to lookup an HTTP port with
   an HTTP method fn (from above)
   Example:
     (def test-get (http-assert-2/with-port-fn get-http-port http-assert-2/test-get))"
  [port-fn http-fn]
  (fn with-port [path & args]
    (apply (partial http-fn path (port-fn)) args)))
