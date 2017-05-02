(defproject threatgrid/clj-momo "0.2.7-SNAPSHOT"
  :description "Library code produced by the Cisco ThreatGrid team for building swagger backed API services"
  :url "https://github.com/threatgrid/clj-momo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;; logging
                 [org.clojure/tools.logging "0.3.1"]

                 ;; schemas
                 [prismatic/schema "1.1.2"]
                 [metosin/schema-tools "0.9.0"]

                 ;; time
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha1"
                  :exclusions [org.clojure/clojurescript]]
                 [clj-time "0.12.0"]

                 ;; url
                 [com.cemerick/url "0.1.1"]

                 ;;json
                 [cheshire "5.6.3"]

                 ;;http
                 [clj-http "3.4.1"]

                 ;; Metrics
                 [metrics-clojure "2.7.0"]
                 [metrics-clojure-jvm "2.7.0"]
                 [metrics-clojure-ring "2.7.0"]
                 [metrics-clojure-riemann "2.7.0"]
                 [clout "2.1.2"]
                 [slugger "1.0.1"]
                 [riemann-clojure-client "0.4.2"]]
  :main nil

  :codox {:output-path "doc"}

  :plugins [[lein-codox "0.9.6"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :resource-paths ["test/resources"]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
