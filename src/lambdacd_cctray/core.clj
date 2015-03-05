(ns lambdacd-cctray.core
  (:require [clojure.data.xml :as xml]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))



(defn- projects-for [def state]
  [(xml/element :Project {:name "Some Name"
                          :activity "Sleeping"
                          :lastBuildStatus "Exception"
                          :lastBuildLabel "8"
                          :lastBuildTime "2005-09-28T10:30:34+01:00"
                          :webUrl "some-host/some-path/"} [])])

(defn cctray-xml-for [pipeline-def pipeline-state]
  (xml/emit-str
    (xml/element :Projects {} (projects-for pipeline-def pipeline-state))))