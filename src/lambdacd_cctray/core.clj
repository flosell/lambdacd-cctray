(ns lambdacd-cctray.core
  (:require [clojure.data.xml :as xml]
            [lambdacd.presentation :as lp]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn- has-step-id [step-id [k v]]
  (get v step-id)
)

(defn state-for [step-id [k v]]
  (let [build-number k
        status (get v step-id)
        activity (if (= (:status status) :running) "Building" "Sleeping")]
    {:build-number build-number
     :activity activity}))

(defn states-for [step-id state]
  (let [builds-with-step-id (filter #(has-step-id step-id %) (seq state))
        by-most-recent (reverse (sort-by first builds-with-step-id))]
    (map #(state-for step-id %) by-most-recent)))

(defn- project-for [state step-info]
  (let [states-for-step (states-for (:step-id step-info) state)
        state-for-step (first states-for-step)]
    (xml/element :Project {:name (:name step-info)
                           :activity (:activity state-for-step)
                           :lastBuildStatus "Exception"
                           :lastBuildLabel (str (:build-number state-for-step))
                           :lastBuildTime "2005-09-28T10:30:34+01:00"
                           :webUrl "some-host/some-path/"} [])))


(defn- projects-for [def state]
  (let [pipeline-representation (lp/display-representation def)]
    (map (partial project-for state) pipeline-representation)))

(defn cctray-xml-for [pipeline-def pipeline-state]
  (xml/emit-str
    (xml/element :Projects {} (projects-for pipeline-def pipeline-state))))

(defn cctray-handler-for [pipeline-def state-atom]
  {:status  200
   :headers {"Content-Type" "application/xml"}
   :body    (cctray-xml-for pipeline-def @state-atom)})

