(ns clj-momo.test-helpers.http
  (:refer-clojure :exclude [get])
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clj-momo.lib.url :as url]
            [clojure
             [edn :as edn]
             [pprint :refer [pprint]]
             [string :as str]]))

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
