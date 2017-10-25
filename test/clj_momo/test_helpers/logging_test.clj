(ns clj-momo.test-helpers.logging-test
  (:require [clj-momo.test-helpers.logging :as sut]
            [clojure.test :refer [is deftest testing]]
            [clojure.tools.logging :as log]))

(deftest test-with-test-logging
  (testing "with only `last-log-atom` (backwards compat)"
    (sut/with-test-logging [#{:warn} last-log-atom]

      (log/warn "Test warning log 1")
      (log/error "Test error log")
      (log/warn "Test warning log 2")
      (log/info "Test info log")

      (testing "warning logs can be tested"
        (let [[_ _ _ last-message] @last-log-atom]

          (is (= "Test warning log 2" last-message))))))

  (testing "with `all-logs-atom`"
    (sut/with-test-logging [#{:warn} last-log-atom agent-used-atom? all-logs-atom]

      (log/warn "Test warning log 1")
      (log/error "Test error log")
      (log/warn "Test warning log 2")
      (log/info "Test info log")

      (testing "warning logs can be tested"
        (let [[_ _ _ last-message] @last-log-atom
              messages (map #(nth % 3) @all-logs-atom)]

          (is (false? @agent-used-atom?))
          (is (= "Test warning log 2" last-message))
          (is (= ["Test warning log 1"
                  "Test warning log 2"] messages)))))))

(deftest test-find-log
  (testing "can find a log message by regex"
    (sut/with-test-logging [#{:warn} _ _ all-logs-atom]

      (log/warn "Test warning log 1")
      (log/error "Test error log")
      (log/warn "Test warning log 2")
      (log/info "Test info log")

      (testing "warning logs can be tested"
        (is (true?  (sut/was-logged? #"warning log 1" all-logs-atom)))
        (is (true?  (sut/was-logged? #"warning log 2" all-logs-atom)))
        (is (false? (sut/was-logged? #"foo log"       all-logs-atom)))
        (is (false? (sut/was-logged? #"error log"     all-logs-atom)))
        (is (false? (sut/was-logged? #"info log"      all-logs-atom)))))

    (sut/with-test-logging [#{:warn :error} _ _ all-logs-atom]

      (log/warn "Test warning log 1")
      (log/error "Test error log")
      (log/warn "Test warning log 2")
      (log/info "Test info log")

      (testing "warning logs can be tested"
        (is (true?  (sut/was-logged? #"warning log 1" all-logs-atom)))
        (is (true?  (sut/was-logged? #"warning log 2" all-logs-atom)))
        (is (false? (sut/was-logged? #"foo log"       all-logs-atom)))
        (is (true?  (sut/was-logged? #"error log"     all-logs-atom)))
        (is (false? (sut/was-logged? #"info log"      all-logs-atom)))))))
