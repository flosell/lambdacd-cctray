(defproject lambdacd-cctray "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lambdacd "0.1.0-alpha12"]
                 [clj-time "0.9.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :profiles {:dev {:dependencies [[clj-cctray "0.8.0"]]}})
