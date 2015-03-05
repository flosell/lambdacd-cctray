(ns lambdacd-cctray.core
  (:require [clojure.data.xml :as xml]
            [lambdacd.presentation :as lp]))


(defn- has-step-id [step-id [k v]]
  (get v step-id)
)

(defn state-for [step-id [k v]]
  (let [build-number k
        status (get v step-id)
        activity (if (= (:status status) :running) "Building" "Sleeping")]
    {:build-number build-number
     :activity activity
     :result status}))

(defn- states-for [step-id state]
  (let [builds-with-step-id (filter #(has-step-id step-id %) (seq state))
        by-most-recent (reverse (sort-by first builds-with-step-id))]
    (map #(state-for step-id %) by-most-recent)))

(defn- last-build-status-for [states-for-step]
  (let [current-build (:result (first states-for-step))
        prev-build (:result (second states-for-step))
        cur-build-status (:status current-build)
        build-to-use (if (or (= cur-build-status :running) (= cur-build-status :waiting))
                       prev-build
                       current-build)
        status-to-use (:status build-to-use)]
    (case status-to-use
      :success "Success"
      :failure "Failure"
      "Unknown")))

(defn- project-for [state step-info]
  (let [states-for-step (states-for (:step-id step-info) state)
        state-for-step (first states-for-step)]
    (xml/element :Project {:name (:name step-info)
                           :activity (:activity state-for-step)
                           :lastBuildStatus (last-build-status-for states-for-step)
                           :lastBuildLabel (str (:build-number state-for-step))
                           :lastBuildTime "2005-09-28T10:30:34+01:00"
                           :webUrl "some-host/some-path/"} [])))

(defn- flatten-pipeline [pipeline-representation]
  (let [children-reps (flatten (map #(flatten-pipeline (:children %)) pipeline-representation))]
    (concat pipeline-representation children-reps)))

(defn- projects-for [def state]
  (let [pipeline-representation (flatten-pipeline (lp/display-representation def))]
    (map (partial project-for state) pipeline-representation)))

(defn cctray-xml-for [pipeline-def pipeline-state]
  (xml/emit-str
    (xml/element :Projects {} (projects-for pipeline-def pipeline-state))))

(defn cctray-handler-for [pipeline-def state-atom]
  (fn [& _]
    {:status  200
     :headers {"Content-Type" "application/xml"}
     :body    (cctray-xml-for pipeline-def @state-atom)}))

