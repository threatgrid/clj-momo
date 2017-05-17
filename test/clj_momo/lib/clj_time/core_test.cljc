(ns clj-momo.lib.clj-time.core-test
  (:require [clj-momo.lib.clj-time.core :as sut]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(deftest date-time-protocol-delegate-test
  (testing "year"
    (is (= 2017 (sut/year (sut/date-time 2017)))))

  (testing "month"
    (is (= 5 (sut/month (sut/date-time 2017 5)))))

  (testing "day"
    (is (= 16 (sut/day (sut/date-time 2017 5 16)))))

  (testing "day-of-week"
    (is (= 2 (sut/day-of-week (sut/date-time 2017 5 16)))))

  (testing "hour"
    (is (= 19 (sut/hour (sut/date-time 2017 5 16 19)))))

  (testing "minute"
    (is (= 22 (sut/minute (sut/date-time 2017 5 16 19 22)))))

  (testing "second"
    (is (= 42 (sut/second (sut/date-time 2017 5 16 19 23 42)))))

  (testing "milli"
    (is (= 999 (sut/milli (sut/date-time 2017 5 16 19 29 42 999)))))

  (testing "equal?"
    (is (sut/equal?
         (sut/date-time 2017 5 16 19 29 42 999)
         (sut/date-time 2017 5 16 19 29 42 999)))

    (is (not
         (sut/equal?
          (sut/date-time 2017 5 16 19 29 42 999)
          nil))))

  (testing "after?"
    (is (sut/after?
         (sut/date-time 2017 5 16 19 29 42 999)
         (sut/date-time 2017 5 16 19 23 42))))

  (testing "before?"
    (is (sut/before?
         (sut/date-time 2017 5 16 19 23 42)
         (sut/date-time 2017 5 16 19 29 42 999))))

  (testing "plus"
    (is (sut/equal?
         (sut/date-time 1986 12 5)
         (sut/plus (sut/date-time 1986 10 14)
                   (sut/months 1)
                   (sut/weeks 3)))))

  (testing "minus"
    (is (sut/equal?
         (sut/date-time 2017 5)
         (sut/minus (sut/date-time 2017 6)
                    (sut/months 1)))))

  (testing "first-day-of-the-month"
    (is (sut/equal?
         ;; TODO: CLJ and CLJS seem to behave differently.  Try to fix this?
         #?(:clj  (sut/date-time 2017 5 1 19)
            :cljs (sut/date-time 2017 5 1))
         (sut/first-day-of-the-month
          (sut/date-time 2017 5 16 19)))))

  (testing "last-day-of-the-month"
    (is (sut/equal?
         ;; TODO: CLJ and CLJS seem to behave differently.  Try to fix this?
         #?(:clj  (sut/date-time 2017 5 31 19)
            :cljs (sut/date-time 2017 5 31))
         (sut/last-day-of-the-month
          (sut/date-time 2017 5 16 19)))))

  (testing "week-number-of-year"
    (is (= 42
           (sut/week-number-of-year
            (sut/date-time 2017 10 16))))))

#?(:clj
   (deftest date-time-protocol-internal-date-test
     (testing "java.util.Date"
       (testing "year"
         (is (= 2017 (sut/year (sut/internal-date 2017)))))

       (testing "month"
         (is (= 5 (sut/month (sut/internal-date 2017 5)))))

       (testing "day"
         (is (= 16 (sut/day (sut/internal-date 2017 5 16)))))

       (testing "day-of-week"
         (is (= 2 (sut/day-of-week (sut/internal-date 2017 5 16)))))

       (testing "hour"
         (is (= 19 (sut/hour (sut/internal-date 2017 5 16 19)))))

       (testing "minute"
         (is (= 22 (sut/minute (sut/internal-date 2017 5 16 19 22)))))

       (testing "second"
         (is (= 42 (sut/second (sut/internal-date 2017 5 16 19 23 42)))))

       (testing "milli"
         (is (= 999 (sut/milli (sut/internal-date 2017 5 16 19 29 42 999)))))

       (testing "equal?"
         (is (sut/equal?
              (sut/internal-date 2017 5 16 19 29 42 999)
              (sut/internal-date 2017 5 16 19 29 42 999))))

       (testing "after?"
         (is (sut/after?
              (sut/internal-date 2017 5 16 19 29 42 999)
              (sut/internal-date 2017 5 16 19 23 42))))

       (testing "before?"
         (is (sut/before?
              (sut/internal-date 2017 5 16 19 23 42)
              (sut/internal-date 2017 5 16 19 29 42 999))))

       (testing "plus"
         (is (sut/equal?
              (sut/internal-date 1986 12 5)
              (sut/plus (sut/internal-date 1986 10 14)
                        (sut/months 1)
                        (sut/weeks 3)))))

       (testing "minus"
         (is (sut/equal?
              (sut/internal-date 2017 5)
              (sut/minus (sut/internal-date 2017 6)
                         (sut/months 1)))))

       (testing "first-day-of-the-month"
         (is (sut/equal?
              (sut/internal-date 2017 5 1 19)
              (sut/first-day-of-the-month
               (sut/internal-date 2017 5 16 19)))))

       (testing "last-day-of-the-month"
         (is (sut/equal?
              (sut/internal-date 2017 5 31 19)
              (sut/last-day-of-the-month
               (sut/internal-date 2017 5 16 19)))))

       (testing "week-number-of-year"
         (is (= 42
                (sut/week-number-of-year
                 (sut/internal-date 2017 10 16))))))))

#?(:clj
   (deftest within?-test
     (testing "org.joda.time.DateTime"
       (let [interval (sut/interval (sut/date-time 2017 5 1)
                                    (sut/date-time 2017 5 31))
             test (sut/date-time 2017 5 19)]
         (is
          (sut/within? interval test)))

       (let [start (sut/date-time 2017 5 1)
             end (sut/date-time 2017 5 31)
             test (sut/date-time 2017 5 19)]
         (is
          (sut/within? start end test))))

     (testing "java.util.date"
       (let [interval (sut/interval (sut/date-time 2017 5 1)
                                    (sut/date-time 2017 5 31))
             test (sut/internal-date 2017 5 19)]
         (is
          (sut/within? interval test)))

       (let [start (sut/date-time 2017 5 1)
             end (sut/date-time 2017 5 31)
             test (sut/internal-date 2017 5 19)]
         (is
          (sut/within? start end test))))))
