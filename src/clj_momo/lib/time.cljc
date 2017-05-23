(ns ^{:doc "Work with java.util.Date objects"}
    clj-momo.lib.time
  (:require [clj-momo.lib.clj-time.core :as time]
            [clj-momo.lib.clj-time.format :as time-format]
            [clj-momo.lib.clj-time.coerce :as time-coerce]
            [clj-momo.lib.clj-time.periodic :refer [periodic-seq]])

  #?(:clj (:import [java.sql Time Timestamp]
                   [java.util Date]
                   [org.joda.time DateTime DateTimeZone])))

(def ^:deprecated coerce-to-datetime time-coerce/to-date-time)

(def ^:deprecated coerce-to-date time-coerce/to-date)

(defn timestamp
  ([] (time/internal-now))
  ([time-str]
   (if (nil? time-str)
     (time/internal-now)
     (time-coerce/to-internal-date time-str))))

(def ^:deprecated now time/internal-now)

(def ^:deprecated default-expire-date
  ;; This is moved to ctim.domain.time
  (time/internal-date 2525 1 1))

(def ^:deprecated after? time/after?)

(def period-fns
  {:years time/years
   :months time/months
   :weeks time/weeks
   :days time/days
   :hours time/hours
   :minutes time/minutes
   :seconds time/seconds})

(defn ^:deprecated plus-n [p t n]
  ;; It is preferable to use the time lib directly
  (let [period-fn (get period-fns p)]
    (when period-fn
      (time/plus t
                 (period-fn n)))))

(defn ^:deprecated plus-n-weeks [t n]
  ;; It is preferable to use the time lib directly
  (time/plus t (time/weeks n)))

(defn format-date-time [d]
  (->> d
       (time-coerce/from-date)
       (time-format/unparse (time-format/formatters :date-time))))

(defn format-index-time [d]
  (->> d
       (time-coerce/from-date)
       (time-format/unparse (time-format/formatter "YYYY.MM.dd.HH.mm"))))

(defn format-rfc822-time [d]
  (->> d
       (time-coerce/from-date)
       (time-format/unparse (time-format/formatters :rfc822))))

#?(:clj
   (defn round-date [d granularity]
     (let [year (time/year d)
           month (time/month d)
           day (time/day d)
           hour (time/hour d)
           minute (time/minute d)]

       (case granularity
         :week (-> (time/date-time year month day)
                   ;; This is joda DateTime specific
                   (.dayOfWeek)
                   (.withMinimumValue)
                   (time-coerce/to-internal-date))
         :minute (time/internal-date year month day hour minute)
         :hour   (time/internal-date year month day hour)
         :day    (time/internal-date year month day)
         :month  (time/internal-date year month)
         :year   (time/internal-date year)))))

(defn date-range [start end step]
  (let [inf-range (periodic-seq start step)
        below-end? (fn [t] (time/within? (time/interval start end) t))]
    (take-while below-end? inf-range)))

(defn date-str->valid-time
  ([date-str offset]
   (date-str->valid-time date-str offset :days))
  ([date-str offset p]
   "Create a ctim.schemas.common/ValidTime from a date str and an offset"
   (let [start (time-coerce/to-internal-string date-str)
         period-fn (period-fns p)]
     (when period-fn
       {:start_time start
        :end_time (time/plus start (period-fn offset))}))))
