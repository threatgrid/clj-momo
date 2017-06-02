(ns clj-momo.lib.clj-time.coerce
  #?(:cljs (:require-macros [clj-momo.lib.clj-time.macros :refer [immigrate]]))
  (:require #?(:clj [clj-momo.lib.clj-time.macros :refer [immigrate]])
            [clj-momo.lib.clj-time.format :as time-fmt]
            #?(:clj  [clj-time.coerce :as coerce-delegate]
               :cljs [cljs-time.coerce :as coerce-delegate]))
  (:import #?(:clj [java.util Date])))

(declare to-internal-date)

(defprotocol ICoerceCustom
  (to-date-time [obj])
  (to-date [obj])
  (to-internal-string [obj]))

(immigrate coerce-delegate
           [from-long
            from-date
            to-long
            to-epoch
            to-string
            to-local-date
            to-local-date-time])

#?(:clj
   (immigrate coerce-delegate
              [from-sql-date
               from-sql-time
               in-time-zone
               to-sql-date
               to-sql-time
               to-timestamp]))

(def internal-date-formatter
  ;; ISO 8601
  (time-fmt/formatters :date-time))

(defn unparse-internal-date [internal-date]
  #?(:clj (time-fmt/unparse internal-date-formatter (to-date-time internal-date))
     :cljs (time-fmt/unparse internal-date-formatter internal-date)))

(defn parse-internal-string [internal-string]
  #?(:clj (to-internal-date
           (time-fmt/parse internal-date-formatter internal-string))
     :cljs (time-fmt/parse internal-date-formatter internal-string)))

(def from-string
  "This is similar to coerce-delegate/from-string, but is optimized to prefer
  the :date-time format (it tries it first)."
  (let [formatters (cons (:date-time time-fmt/formatters)
                         (->> time-fmt/formatters
                              (remove (fn [[k v]]
                                        (= k :date-time)))
                              (map last)))]
    (fn [s]
      (when s
        (first
         (for [f formatters
               :let [d (try
                         (time-fmt/parse f s)
                         #?(:clj  (catch Exception _ nil)
                            :cljs (catch js/Error _ nil)))]
               :when d]
           d))))))

#?(:clj
   (extend-protocol ICoerceCustom
     nil
     (to-date-time [_]
       nil)
     (to-date [_]
       nil)
     (to-internal-string [_]
       nil)

     java.util.Date
     (to-date-time [date]
       (coerce-delegate/from-date date))
     (to-date [date]
       date)
     (to-internal-string [date]
       (unparse-internal-date date))

     java.sql.Date
     (to-date-time [sql-date]
       (coerce-delegate/from-sql-date sql-date))
     (to-date [sql-date]
       (-> sql-date to-date-time to-date))
     (to-internal-string [sql-date]
       (-> sql-date to-date-time to-internal-string))

     ;; As far as I can tell, clj-time implements Timestamp twice and
     ;; in two different ways.  I think that is a bug.
     java.sql.Timestamp
     (to-date-time [sql-time]
       (coerce-delegate/from-sql-time sql-time))
     (to-date [sql-time]
       (-> sql-time to-date-time to-date))
     (to-internal-string [sql-time]
       (-> sql-time to-date-time to-internal-string))

     org.joda.time.DateTime
     (to-date-time [date-time]
       date-time)
     (to-date [date-time]
       (Date. (.getMillis date-time)))
     (to-internal-string [date-time]
       (time-fmt/unparse internal-date-formatter date-time))

     org.joda.time.YearMonth
     (to-date-time [year-month]
       (coerce-delegate/to-date-time year-month))
     (to-date [year-month]
       (-> year-month to-date-time to-date))
     (to-internal-string [year-month]
       (-> year-month to-date-time to-internal-string))

     org.joda.time.LocalDate
     (to-date-time [local-date]
       (coerce-delegate/to-date-time local-date))
     (to-date [local-date]
       (-> local-date to-date-time to-date))
     (to-internal-string [local-date]
       (-> local-date to-date-time to-internal-string))

     org.joda.time.LocalDateTime
     (to-date-time [local-date-time]
       (coerce-delegate/to-date-time local-date-time))
     (to-date [local-date-time]
       (-> local-date-time to-date-time to-date))
     (to-internal-string [local-date-time]
       (-> local-date-time to-date-time to-internal-string))

     java.lang.Integer
     (to-date-time [integer]
       (coerce-delegate/from-long (long integer)))
     (to-date [integer]
       (Date. (long integer)))
     (to-internal-string [integer]
       (-> integer to-date-time to-internal-string))

     java.lang.Long
     (to-date-time [long]
       (coerce-delegate/from-long long))
     (to-date [long]
       (Date. long))
     (to-internal-string [long]
       (-> long to-date-time to-internal-string))

     java.lang.String
     (to-date-time [string]
       (from-string string))
     (to-date [string]
       (-> string to-date-time to-date))
     (to-internal-string [string]
       (-> string to-date-time to-internal-string)))

   :cljs
   (extend-protocol ICoerceCustom
     nil
     (to-date-time [_]
       nil)
     (to-date [_]
       nil)
     (to-internal-string [_]
       nil)

     js/Date
     (to-date-time [date]
       (coerce-delegate/from-date date))
     (to-date [date]
       date)
     (to-internal-string [date]
       (-> date to-date-time to-internal-string))

     goog.date.Date
     (to-date-time [local-date]
       (coerce-delegate/to-date-time local-date))
     (to-date [local-date]
       (-> local-date to-date-time to-date))
     (to-internal-string [local-date]
       (-> local-date to-date-time to-internal-string))

     goog.date.DateTime
     (to-date-time [local-date-time]
       (coerce-delegate/to-date-time local-date-time))
     (to-date [local-date-time]
       (-> local-date-time to-date-time to-date))
     (to-internal-string [local-date-time]
       (-> local-date-time to-date-time to-internal-string))

     goog.date.UtcDateTime
     (to-date-time [date-time]
       date-time)
     (to-date [date-time]
       (-> date-time .getTime js/Date.))
     (to-internal-string [date-time]
       (unparse-internal-date date-time))

     number
     (to-date-time [long]
       (coerce-delegate/from-long long))
     (to-date [long]
       (-> long to-date-time to-date))
     (to-internal-string [long]
       (-> long to-date-time to-internal-string))

     string
     (to-date-time [string]
       (from-string string))
     (to-date [string]
       (-> string to-date-time to-date))
     (to-internal-string [string]
       (-> string to-date-time to-internal-string))))

(def to-internal-date
  #?(:clj to-date
     :cljs to-date-time))
