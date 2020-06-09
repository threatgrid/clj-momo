(def clj-version "1.10.1")
(def metrics-clojure-version "2.10.0")

(defproject threatgrid/clj-momo "0.4.0-SNAPSHOT"
  :description "Library code produced by the Cisco ThreatGrid team for building swagger backed API services"
  :url "https://github.com/threatgrid/clj-momo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :pedantic? :abort
  ; when changing, use `lein pom; mvn dependency:tree -Dverbose` to print
  ; full deps tree and understand all conflicts.
  ; Also see `https://github.com/clj-commons/vizdeps` for visual representation
  ; (may require a local install, not on clojars).
  ; Use :exclusions only to permanently delete a dependency, and document reasons.
  ; Avoid combining :exclusions with an override as it can obscure the indended
  ; dependencies when browsing the full deps tree.
  :dependencies [[org.clojure/clojure ~clj-version]
                 [org.clojure/tools.logging "0.5.0"]
                 [prismatic/schema "1.1.12"]
                 [metosin/schema-tools "0.12.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clj-time "0.15.2"]
                 [com.arohner/uri "0.1.2"]
                 [cheshire "5.9.0"]
                 [clj-http "3.10.1"]
                 [slugger "1.0.1"]
                 [riemann-clojure-client "0.5.1"]
                 [metrics-clojure ~metrics-clojure-version]
                 [metrics-clojure-jvm ~metrics-clojure-version]
                 [metrics-clojure-ring ~metrics-clojure-version]
                 [threatgrid/metrics-clojure-riemann "2.10.1"] ; uses io.riemann.* classes instead of com.aphyr
                 [clout "2.2.1"]]

  :main nil

  :codox {:output-path "doc"
          :source-paths ["src"]}

  ; use `lein deps :plugin-tree` to debug
  :plugins [[org.clojure/clojure ~clj-version] ;update lein-cljsbuild and lein-doo's dep
            [lein-cljsbuild "1.1.7"]
            [lein-codox "0.10.7"]
            [com.google.guava/guava "20.0"] ;fix some unknown internal conflict in `doo`
            [doo "0.1.11"] ;update lein-doo's dep
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

  :global-vars {*warn-on-reflection* true}

  :profiles {:dev {:dependencies [;https://clojure.atlassian.net/browse/CLJS-3047
                                  [com.google.errorprone/error_prone_annotations "2.1.3"]
                                  ;https://clojure.atlassian.net/browse/CLJS-3047
                                  [com.google.code.findbugs/jsr305 "3.0.2"]
                                  [org.clojure/clojurescript "1.10.597"]
                                  [ch.qos.logback/logback-classic "1.2.3"]]
                   :resource-paths ["test/resources"]}})
