(ns clj-momo.ring.middleware.metrics
  "Middleware to control all metrics of the server"
  (:require [clout.core :as clout]
            [metrics
             [core :refer [default-registry remove-metric]]
             [meters :refer [mark! meter]]
             [timers :refer [time! timer]]]
            [metrics.jvm.core :as jvm]
            [metrics.ring.instrument :refer [instrument]]
            [slugger.core :refer [->slug]]))


(def ^:private add-default-metrics
  (let [done? (volatile! false)
        lock (Object.)]
    (fn []
      "This should only ever be done once"
      (locking lock
        (when-not @done?
          (jvm/register-memory-usage-gauge-set default-registry)
          (jvm/register-garbage-collector-metric-set default-registry)
          (jvm/register-thread-state-gauge-set default-registry)
          (vreset! done? true))))))

(defn match-route? [[compiled-path _ verb] request]
  (if (= (name (:request-method request)) verb)
    (some? (clout/route-matches compiled-path request))
    false))

(defn matched-route [routes request]
  (first (filter #(match-route? % request) routes)))

(defn gen-metrics-for [handler routes prefix]
  (let [reg default-registry
        time-str (str prefix "-time")
        req-str (str prefix "-req")
        ;; Time by swagger route
        times (reduce (fn [acc [_ path verb]]
                        (assoc-in acc [path verb]
                                  (timer reg
                                         [time-str path verb])))
                      {:unregistered (timer reg
                                            [time-str "_" "unregistered"])}
                      routes)
        ;; Meter by swagger route
        meters (reduce (fn [acc [_ path verb]]
                         (assoc-in acc [path verb]
                                   (meter reg [req-str path verb])))
                       {:unregistered (meter reg [req-str "_" "unregistered"])}
                       routes)]
    (fn [request]
      (let [route (or (matched-route routes request)
                      [:place_holder :unregistered])]
        (mark! (get-in meters (drop 1 route)))
        (time! (get-in times (drop 1 route)) (handler request))))))


;; The get-routes-fn probably comes form compojure.api.routes, but we
;; want to avoid adding that dependancy to clj-momo.

(defn wrap-metrics [prefix get-routes-fn]
  (fn [handler]
    (let [routes (get-routes-fn handler)
          exposed-routes (map (fn [l] [(clout/route-compile (first l))
                                       (->slug (first l))
                                       (name (second l))])
                              routes)]
      (add-default-metrics)
      (-> handler
          (instrument)
          (gen-metrics-for exposed-routes prefix)))))
