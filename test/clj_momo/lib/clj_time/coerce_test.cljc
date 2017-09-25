(ns clj-momo.lib.clj-time.coerce-test
  (:require [clojure.string :as str]
            [clj-momo.lib.clj-time.coerce :as sut]
            [clj-momo.lib.clj-time.core :as core]
            #?(:clj  [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))


(deftest to-date-time-test
  (testing "to-date-time"
    (testing "nil"
      (is (nil? (sut/to-date-time nil))))

    (testing "long"
      (is
       (core/equal?
        (core/date-time 2017 5 19)
        (sut/to-date-time 1495152000000))))

    (testing "string"
      (is
       (core/equal?
        (core/date-time 2017 5 19 5 17 24 894)
        (sut/to-date-time "20170519T051724.894Z")))

      (is
       (core/equal?
        (core/date-time 2017 5 19 4 34 44)
        (sut/to-date-time "Fri, 19 May 2017 04:34:44 +0000"))))

    (testing "identity"
      (is
       (core/equal?
        (core/date-time 2017 5 19)
        (sut/to-date-time
         (core/date-time 2017 5 19)))))

    #?(:clj
       (testing "CLJ"
         (testing "java.util.Date"
           (is
            (core/equal?
             (core/date-time 2017 5 19)
             (sut/to-date-time
              (java.util.Date. 1495152000000)))))

         (testing "java.sql.Date"
           (is
            (core/equal?
             (core/date-time 2017 5 19)
             (sut/to-date-time
              (java.sql.Date. 1495152000000)))))

         (testing "java.sql.Timestamp"
           (is
            (core/equal?
             (core/date-time 2017 5 19)
             (sut/to-date-time
              (java.sql.Timestamp. 1495152000000)))))

         (testing "org.joda.time.YearMonth"
           (is
            (core/equal?
             (core/date-time 2017 5 1)
             (sut/to-date-time
              (org.joda.time.YearMonth. 2017 5)))))

         (testing "org.joda.time.LocalDate"
           (is
            (core/equal?
             (core/date-time 2017 5 18)
             (sut/to-date-time
              (org.joda.time.LocalDate.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "org.joda.time.LocalDateTime"
           (is
            (core/equal?
             (core/date-time 2017 5 18 17)
             (sut/to-date-time
              (org.joda.time.LocalDateTime.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "java.lang.Integer"
           (is
            (core/equal?
             (core/date-time 1970 01 25 20 31 23 647)
             (sut/to-date-time (int 2147483647))))))

       :cljs
       (testing "CLJS"
         (testing "js/Date"
           (is
            (core/equal?
             (core/date-time 2017 5 19)
             (sut/to-date-time (js/Date. 1495152000000)))))

         (testing "goog.date.Date"
           ;; avoid time zone offset
           (let [local-date (goog.date.Date. 2017 4 19)
                 dt (sut/to-date-time local-date)]
             (is (= 2017 (core/year dt)))
             (is (= 5 (core/month dt)))
             (is (= 19 (core/day dt)))))

         (testing "goog.date.DateTime"
           ;; avoid time zone offset
           (let [local-date-time (goog.date.DateTime. 2017 4 19)
                 dt (sut/to-date-time local-date-time)]
             (is (= 2017 (core/year dt)))
             (is (= 5 (core/month dt)))
             (is (= 19 (core/day dt)))))))))

(deftest to-internal-date-test
  (testing "to-internal-date"

    (testing "nil"
      (is (nil? (sut/to-internal-date nil))))

    (testing "long"
      (is
       (core/equal?
        (core/internal-date 2017 5 19)
        (sut/to-internal-date 1495152000000))))

    (testing "string"
      (is
       (core/equal?
        (core/internal-date 2017 5 19 5 17 24 894)
        (sut/to-internal-date "20170519T051724.894Z")))

      (is
       (core/equal?
        (core/internal-date 2017 5 19 4 34 44)
        (sut/to-internal-date "Fri, 19 May 2017 04:34:44 +0000"))))

    (testing "identity"
      (is
       (core/equal?
        (core/internal-date 2017 5 19)
        (sut/to-internal-date
         (core/internal-date 2017 5 19)))))

    #?(:clj
       (testing "CLJ"

         (testing "java.joda.time.DateTime"
           (is
            (core/equal?
             (core/internal-date 2017 5 19)
             (sut/to-internal-date
              (core/date-time 2017 5 19)))))

         (testing "java.sql.date"
           (is
            (core/equal?
             (core/internal-date 2017 5 19)
             (sut/to-internal-date
              (java.sql.Date. 1495152000000)))))

         (testing "java.sql.Timestamp"
           (is
            (core/equal?
             (core/internal-date 2017 5 19)
             (sut/to-internal-date
              (java.sql.Timestamp. 1495152000000)))))

         (testing "org.joda.time.YearMonth"
           (is
            (core/equal?
             (core/internal-date 2017 5 1)
             (sut/to-internal-date
              (org.joda.time.YearMonth. 2017 5)))))

         (testing "org.joda.time.LocalDate"
           (is
            (core/equal?
             (core/internal-date 2017 5 18)
             (sut/to-internal-date
              (org.joda.time.LocalDate.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "org.joda.time.LocalDateTime"
           (is
            (core/equal?
             (core/internal-date 2017 5 18 17)
             (sut/to-internal-date
              (org.joda.time.LocalDateTime.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "java.lang.Integer"
           (is
            (core/equal?
             (core/internal-date 1970 01 25 20 31 23 647)
             (sut/to-internal-date (int 2147483647))))))

       :cljs
       (testing "CLJS"

         (testing "js/Date"
           (is
            (core/equal?
             (core/internal-date 2017 5 19)
             (sut/to-internal-date (js/Date. 1495152000000)))))

         (testing "goog.date.Date"
           ;; avoid time zone offset
           (let [local-date (goog.date.Date. 2017 4 19)
                 dt (sut/to-internal-date local-date)]
             (is (= 2017 (core/year dt)))
             (is (= 5 (core/month dt)))
             (is (= 19 (core/day dt)))))

         (testing "goog.date.DateTime"
           ;; avoid time zone offset
           (let [local-date-time (goog.date.DateTime. 2017 4 19)
                 dt (sut/to-internal-date local-date-time)]
             (is (= 2017 (core/year dt)))
             (is (= 5 (core/month dt)))
             (is (= 19 (core/day dt)))))

         (testing "goog.date.UtcDateTime"
           (is
            (core/equal?
             (core/internal-date 2017 5 19)
             (sut/to-internal-date
              (core/date-time 2017 5 19)))))))))

(deftest to-internal-string-test
  (testing "to-internal-string"

    (testing "nil"
      (is (= nil (sut/to-internal-string nil))))

    (testing "long"
      (is
       (=
        "2017-05-19T00:00:00.000Z"
        (sut/to-internal-string 1495152000000))))

    (testing "string"
      (is
       (=
        "2017-05-19T05:17:24.894Z"
        (sut/to-internal-string "20170519T051724.894Z")))

      (is
       (=
        "2017-05-19T04:34:44.000Z"
        (sut/to-internal-string "Fri, 19 May 2017 04:34:44 +0000"))))

    (testing "identity"
      (is
       (= "2017-05-19T05:17:24.894Z"
          (sut/to-internal-string "2017-05-19T05:17:24.894Z"))))

    #?(:clj
       (testing "CLJ"

         (testing "java.joda.time.DateTime"
           (is
            (=
             "2017-05-19T00:00:00.000Z"
             (sut/to-internal-string
              (core/date-time 2017 5 19)))))

         (testing "java.sql.date"
           (is
            (=
             "2017-05-19T00:00:00.000Z"
             (sut/to-internal-string
              (java.sql.Date. 1495152000000)))))

         (testing "java.sql.Timestamp"
           (is
            (=
             "2017-05-19T00:00:00.000Z"
             (sut/to-internal-string
              (java.sql.Timestamp. 1495152000000)))))

         (testing "org.joda.time.YearMonth"
           (is
            (=
             "2017-05-01T00:00:00.000Z"
             (sut/to-internal-string
              (org.joda.time.YearMonth. 2017 5)))))

         (testing "org.joda.time.LocalDate"
           (is
            (=
             "2017-05-18T00:00:00.000Z"
             (sut/to-internal-string
              (org.joda.time.LocalDate.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "org.joda.time.LocalDateTime"
           (is
            (=
             "2017-05-18T17:00:00.000Z"
             (sut/to-internal-string
              (org.joda.time.LocalDateTime.
               1495152000000
               (org.joda.time.DateTimeZone/forID "America/Los_Angeles"))))))

         (testing "java.lang.Integer"
           (is
            (=
             "1970-01-25T20:31:23.647Z"
             (sut/to-internal-string (int 2147483647))))))

       :cljs
       (testing "CLJS"

         (testing "js/Date"
           (is
            (=
             "2017-05-19T00:00:00.000Z"
             (sut/to-internal-string (js/Date. 1495152000000)))))

         (testing "goog.date.Date"
           ;; avoid time zone offset
           (str/starts-with?
            (sut/to-internal-string
             (goog.date.Date. 2017 4 19))
            "2017-05-19"))

         (testing "good.date.DateTime"
           ;; avoid time zone offset
           (str/starts-with?
            (sut/to-internal-string
             (goog.date.DateTime. 2017 4 19))
            "2017-05-19"))

         (testing "goog.date.UtcDateTime"
           (is
            (=
             "2017-05-19T00:00:00.000Z"
             (sut/to-internal-string
              (core/date-time 2017 5 19)))))))))


(comment
  "2017-09-25T20:59:56+00:00"
  "2017-09-25T20:59:56Z"
  "20170925T205956Z"
  )


(deftest internal-date-from-iso8601
  (testing "testing valid date with timezone spec"
    (is (core/equal?
         (sut/to-internal-date "2017-09-25T20:59:56+00:00")
         (sut/internal-date-from-iso8601 "2017-09-25T20:59:56+00:00"))))
  (testing "testing valid date without timezone spec"
    (is (core/equal?
         (sut/to-internal-date "2017-09-25T20:59:56Z")
         (sut/internal-date-from-iso8601 "2017-09-25T20:59:56Z"))))

  (testing "testing compact representation"
    (is (core/equal?
         (sut/to-internal-date "20170925T205956Z")
         (sut/internal-date-from-iso8601 "20170925T205956Z"))))
  
  #?(:cljs
     (testing "testing that it is faster"
       (defn timing [i f]
         (let [start (js/performance.now)]
           (dotimes [_ i]
             (f))
           (- (js/performance.now) start)))
       (let [i 10000
             a #(sut/to-internal-date "2017-02-02T12:56:23.001Z")
             b #(sut/internal-date-from-iso8601 "2017-02-02T12:56:23.001Z")
             ta (timing i a)
             tb (timing i b)]
         (js/console.log "coerce:" ta "parse:" tb "ratio:" (/ ta tb))))))

