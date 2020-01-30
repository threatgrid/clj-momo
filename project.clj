(defproject threatgrid/clj-momo "0.3.4-SNAPSHOT"
  :description "Library code produced by the Cisco ThreatGrid team for building swagger backed API services"
  :url "https://github.com/threatgrid/clj-momo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;; logging
                 [org.clojure/tools.logging "0.5.0"]

                 ;; schemas
                 [prismatic/schema "1.1.12"]
                 [metosin/schema-tools "0.12.2"]

                 ;; time
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clj-time "0.15.2"]

                 ;; uri
                 [com.arohner/uri "0.1.2"]
                 ; for public API in clj-momo.lib.url
                 [com.cemerick/url "0.1.1" :scope "provided"]

                 ;;json
                 [cheshire "5.9.0"]

                 ;;http
                 [clj-http "3.10.0"]

                 ;; Metrics
                 [metrics-clojure "2.10.0"]
                 [metrics-clojure-jvm "2.10.0"]
                 [metrics-clojure-ring "2.10.0"]
                 [metrics-clojure-riemann "2.10.0"]
                 [clout "2.2.1"]
                 [slugger "1.0.1"]
                 [riemann-clojure-client "0.5.1"
                  ;; Protobuf-java is brought in by ClojureScript
                  :exclusions [com.google.protobuf/protobuf-java]]]

  :main nil

  :codox {:output-path "doc"}

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-codox "0.10.7"]
            [lein-doo "0.1.10"]]

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

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/clojurescript "1.10.597"
                                   :exclusions [com.google.errorprone/error_prone_annotations
                                                com.google.code.findbugs/jsr305]]
                                  [ch.qos.logback/logback-classic "1.2.3"]]
                   :resource-paths ["test/resources"]}})
