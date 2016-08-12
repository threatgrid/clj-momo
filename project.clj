(defproject threatgrid/clj-momo "0.2.0"
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
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha1"]
                 [clj-time "0.12.0"]

                 ;; url
                 [com.cemerick/url "0.1.1"]]
  :main nil
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :resource-paths ["test/resources"]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
