(ns clj-momo.test-helpers.http-assert-1
  (:refer-clojure :exclude [get])
  (:require [clj-momo.test-helpers.core]  ;; Make sure deep= is defined
            [clj-momo.test-helpers.http :refer [delete get post put]]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer [is testing]]
            [clojure.tools.logging :as log]))

(defn test-post
  "Like a simplified POST request but with testing points.
  It uses the standard `api-key`.
  It verify that the POST was successful and that the returned entity
  correpond the the one sent.
  In the end the test-post returns only the entity (parsed body)."
  [path port api-key new-entity]
  (let [{status :status
         {:keys [message errors]
          :as result} :parsed-body}
        (post path
              port
              :body new-entity
              :headers {"api_key" api-key})]
    (when message
      (log/error message))
    (when errors
      (log/error errors))
    (is (= 201 status))
    (when (= 201 status)
      (is (= new-entity
             result))
      result)))

(defn assert-post
  "Like test-post, but instead of using (is (= ...)), it only asserts
   that the status is 200.  Useful when the post is for test setup and
   the path is not the subject under test."
  [path port api-key new-entity]
  (let [{status :status
         result :parsed-body
         :as response}
        (post path
              port
              :body new-entity
              :headers {"api_key" api-key})]
    (when (not= 201 status)
      (throw (ex-info (str "Expected status to be 201 but was " status
                           " for " path ":\n"
                           (with-out-str (pprint response)))
                      {:path path
                       :port port
                       :new-entity new-entity
                       :response response})))
    result))

(defn test-put
  "Like a simplified PUT request but with testing points.
  It uses the standard `api-key`.
  It verify that the PUT was successful and that the returned entity
  correpond the the one sent.
  In the end the test-post returns only the entity (parsed body)."
  [path port api-key new-entity]
  (let [resp (put path
                  port
                  :body new-entity
                  :headers {"api_key" api-key})]
    (when (get-in resp [:parsed-body :message])
      (log/error (get-in resp [:parsed-body :message])))
    (when (get-in resp [:parsed-body :errors])
      (log/error (get-in resp [:parsed-body :errors])))
    (is (= 200 (:status resp)))
    (when (= 200 (:status resp))
      (is (= new-entity (dissoc (:parsed-body resp) :id :created :modified :owner)))
      (:parsed-body resp))))

(defn test-get
  "Helper which test a get request occurs with success and return the right object
  Returns the result of the GET call."
  [path port api-key expected-entity]
  (testing (str "GET " path)
    (let [resp (get path
                    port
                    :headers {"api_key" api-key})]
      (when (get-in resp [:parsed-body :message])
        (log/error (get-in resp [:parsed-body :message])))
      (when (get-in resp [:parsed-body :errors])
        (log/error (get-in resp [:parsed-body :errors])))
      (is (= 200 (:status resp)))
      (when (= 200 (:status resp))
        (is (= expected-entity
               (:parsed-body resp)))
        (:parsed-body resp)))))

(defn test-get-list
  "Helper which test a get request occurs with success and returns
  the same list of objects in any order.
  Returns the result of the GET call."
  [path port api-key expected-entities]
  (testing (str "GET " path)
    (let [resp (get path
                    port
                    :headers {"api_key" api-key})]
      (when (get-in resp [:parsed-body :message])
        (log/error (get-in resp [:parsed-body :message])))
      (when (get-in resp [:parsed-body :errors])
        (log/error (get-in resp [:parsed-body :errors])))
      (is (= 200 (:status resp)))
      (when (= 200 (:status resp))
        (is (= (set expected-entities)
               (set (:parsed-body resp))))
        (:parsed-body resp)))))

(defn test-delete
  "Helper which test a delete request occurs with success"
  [path port api-key]
  (testing (str "DELETE " path)
    (let [resp (delete path
                       port
                       :headers {"api_key" api-key})]
      (is (= 204 (:status resp)))
      (= 204 (:status resp)))))

(defn with-port-fn-and-api-key
  "Similar to with-port-fn, but the http-fn has a different signature
   (to match the test-* fns above)"
  [port-fn api-key http-fn]
  (fn
    ([path new-entity]
     (http-fn path (port-fn) api-key new-entity))
    ([path]
     (http-fn path (port-fn) api-key))))
