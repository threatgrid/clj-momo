(ns clj-momo.lib.clj-time.format
  #?(:cljs (:require-macros [clj-momo.lib.clj-time.macros :refer [immigrate]]))
  (:require #?(:clj  [clj-time.format :as format-delegate]
               :cljs [cljs-time.format :as format-delegate])
            #?(:clj [clj-momo.lib.clj-time.macros :refer [immigrate]])))

(immigrate format-delegate
           [formatter
            formatters
            formatter-local
            parse
            parse-local
            parse-local-date
            show-formatters
            unparse
            unparse-local
            unparse-local-date
            with-default-year])

#?(:clj
   (immigrate format-delegate
              [parse-local-time
               unparse-local-time
               with-chronology
               with-default-year
               with-locale
               with-zone]))

(defprotocol Mappable
  (instant->map [instant]))

#?(:clj
   (extend-protocol Mappable
     org.joda.time.DateTime
     (instant->map [instant]
       (format-delegate/instant->map instant))

     org.joda.time.Interval
     (instant->map [instant]
       (format-delegate/instant->map instant))

     org.joda.time.Period
     (instant->map [instant]
       (format-delegate/instant->map instant)))

   :cljs
   (extend-protocol Mappable
     goog.date.UtcDateTime
     (instant->map [instant]
       (format-delegate/instant->map instant))

     cljs-time.core.Period
     (instant->map [instant]
       (format-delegate/instant->map instant))

     cljs-time.core.Interval
     (instant->map [instant]
       (format-delegate/instant->map instant))

     PersistentArrayMap
     (instant->map [instant]
       (format-delegate/instant->map instant))))
