(defproject lambdacd-cctray "0.4.3-SNAPSHOT"
  :description "cctray support for lambdacd"
  :url "http://github.com/flosell/lambdacd-cctray"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:creds :gpg}]
                        ["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [lambdacd "0.11.0"]
                 [clj-time "0.9.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :profiles {:dev {:dependencies [[clj-cctray "0.10.0"]
                                  [ring-server "0.4.0"]]
                   :main lambdacd-cctray.sample-pipeline}})
