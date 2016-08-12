(ns clj-momo.test-helpers.http
  (:refer-clojure :exclude [get])
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clj-momo.lib.url :as url]
            [clojure
             [edn :as edn]
             [pprint :refer [pprint]]
             [string :as str]]
            [clojure.test :refer [is testing]]
            [clojure.tools.logging :as log]))

(defn url [path port]
  (let [url (format "http://localhost:%d/%s" port path)]
    (assert (url/encoded? url)
            (format "URL '%s' is not encoded" url))
    url))

(defn content-type? [expected-str]
  (fn [test-str]
    (if (some? test-str)
      (str/includes? (name test-str) expected-str)
      false)))

(def json? (content-type? "json"))

(def edn? (content-type? "edn"))

(defn parse-body
  ([http-response]
   (parse-body http-response nil))
  ([{{content-type "Content-Type"} :headers
     body :body}
    default]
   (try
     (cond
       (edn? content-type) (edn/read-string body)
       (json? content-type) (json/parse-string body)
       :else default)
     (catch Exception e
       (binding [*out* *err*]
         (println "------- BODY ----------")
         (pprint body)
         (println "------- EXCEPTION ----------")
         (pprint e))))))

(defn encode-body
  [body content-type]
  (cond
    (edn? content-type) (pr-str body)
    (json? content-type) (json/generate-string body)
    :else body))

(defn get [path port & {:as options}]
  (let [options
        (merge {:accept :edn
                :throw-exceptions false
                :socket-timeout 10000
                :conn-timeout 10000}
               options)

        response
        (http/get (url path port)
                  options)]
    (assoc response :parsed-body (parse-body response))))

(defn post [path port & {:as options}]
  (let [{:keys [body content-type]
         :as options}
        (merge {:content-type :edn
                :accept :edn
                :throw-exceptions false
                :socket-timeout 10000
                :conn-timeout 10000}
               options)

        response
        (http/post (url path port)
                   (-> options
                       (cond-> body (assoc :body (encode-body body content-type)))))]
    (assoc response :parsed-body (parse-body response))))

(defn delete [path port & {:as options}]
  (http/delete (url path port)
               (merge {:throw-exceptions false}
                      options)))

(defn put [path port & {:as options}]
  (let [{:keys [body content-type]
         :as options}
        (merge {:content-type :edn
                :accept :edn
                :throw-exceptions false
                :socket-timeout 10000
                :conn-timeout 10000}
               options)

        response
        (http/put (url path port)
                  (-> options
                      (cond-> body (assoc :body (encode-body body content-type)))))]
    (assoc response :parsed-body (parse-body response))))

(defn encode [s]
  (assert (string? s)
          (format "Assert Failed: %s of type %s must be a string"
                  s (type s)))
  (assert (seq s)
          (format "Assert Failed: %s of type %s must be a seq"
                  s (type s)))
  (url/encode s))

(defn decode [s]
  (assert (string? s)
          (format "Assert Failed: %s of type %s must be a string"
                  s (type s)))
  (assert (seq s)
          (format "Assert Failed: %s of type %s must be a seq"
                  s (type s)))
  (url/decode s))

(defn with-port-fn
  "Helper to compose a fn that knows how to lookup an HTTP port with
  an HTTP method fn (from above)
  Example:
    (def post (http/with-port-fn get-http-port http/post))"
  [port-fn http-fn]
  (fn with-port [path & options]
    (apply (partial http-fn path (port-fn)) options)))

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
                           (with-out-str (clojure.pprint/pprint response)))
                      {:path path
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
  (fn with-port-and-api-key
    ([path new-entity]
     (http-fn path (port-fn) api-key new-entity))
    ([path]
     (http-fn path (port-fn) api-key))))
