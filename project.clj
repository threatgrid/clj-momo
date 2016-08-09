(defproject threatgrid/clj-momo "0.1.0-SNAPSHOT"
  :description "Library code produced by the Cisco ThreatGrid team for building swagger backed API services"
  :url "https://github.com/threatgrid/clj-momo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[prismatic/schema "1.1.1"]
                 [metosin/schema-tools "0.7.0"]]
  :main nil
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :resource-paths ["test/resources"]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
