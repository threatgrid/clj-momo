(defproject threatgrid/clj-momo "0.2.20-SNAPSHOT"
  :description "Library code produced by the Cisco ThreatGrid team for building swagger backed API services"
  :url "https://github.com/threatgrid/clj-momo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;; logging
                 [org.clojure/tools.logging "0.3.1"]

                 ;; schemas
                 [prismatic/schema "1.1.7"]
                 [metosin/schema-tools "0.9.1"]

                 ;; time
                 [com.andrewmcveigh/cljs-time "0.5.2"
                  :exclusions [org.clojure/clojurescript]]
                 [clj-time "0.14.2"]

                 ;; url
                 [com.cemerick/url "0.1.1"
                  :exclusions [org.clojure/clojurescript]]

                 ;;json
                 [cheshire "5.8.0"]

                 ;;http
                 [clj-http "3.7.0"]

                 ;; Metrics
                 [metrics-clojure "2.10.0"]
                 [metrics-clojure-jvm "2.10.0"]
                 [metrics-clojure-ring "2.10.0"]
                 [metrics-clojure-riemann "2.10.0"]
                 [clout "2.1.2"]
                 [slugger "1.0.1"]
                 [riemann-clojure-client "0.4.5"
                  ;; Protobuf-java is brought in by ClojureScript
                  :exclusions [com.google.protobuf/protobuf-java]]]
  :main nil

  :codox {:output-path "doc"}

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-codox "0.9.6"]
            [lein-doo "0.1.7"]]

  :cljsbuild {:builds {:node {:source-paths ["src" "test"]
                              :compiler {:output-to "target/tests.js"
                                         :output-dir "target/node"
                                         :optimizations :advanced
                                         :main clj-momo.runner
                                         :pretty-print true
                                         :target :nodejs
                                         :hashbang false}}

                       :test {:source-paths ["src" "test"]
                              :compiler {:output-to "target/tests.js"
                                         :optimizations :whitespace
                                         :main clj-momo.runner
                                         :pretty-print true}}}}

  :test-selectors {:integration :integration
                   :default (complement :integration)
                   :all (constantly true)}

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                                  [org.clojure/clojurescript "1.9.946"]]
                   :resource-paths ["test/resources"]}})
