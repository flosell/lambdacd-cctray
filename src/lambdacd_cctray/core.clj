(ns lambdacd-cctray.core
  (:require [clojure.data.xml :as xml]
            [lambdacd.presentation.pipeline-structure :as lp]
            [lambdacd.state.core :as state]
            [clojure.string :as s]
            [clj-time.format :as f]))

(defn- not-nil? [x]
  (not (nil? x)))

(defn- activity-for [step-result]
  (if (= (:status step-result) :running)
    "Building"
    "Sleeping"))

(defn- state-for [ctx build-number step-id]
  (if-let [step-result (state/get-step-result ctx build-number step-id)]
    {:build-number build-number
     :activity     (activity-for step-result)
     :result       step-result}))

(defn- first-updated [build-state]
  (:first-updated-at (:result build-state)))

(defn- states-for [step-id ctx]
  (->> (state/all-build-numbers ctx)
       (map #(state-for ctx % step-id))
       (filter not-nil?)
       (sort-by first-updated)
       (reverse)))

(defn- cctray-status-for [step-result]
  (case (:status step-result)
    :success "Success"
    :failure "Failure"
    "Unknown"))

(defn- current-build-active? [states-for-step]
  (let [cur-build-status (:status (:result (first states-for-step)))]
    (or (= cur-build-status :running)
        (= cur-build-status :waiting))))

(defn- last-build-status-for [states-for-step]
  (let [build-to-use (if (current-build-active? states-for-step)
                       (second states-for-step)
                       (first states-for-step))]
    (cctray-status-for (:result build-to-use))))

(defn- last-build-time-for [step]
  (let [most-recent-update (:most-recent-update-at (:result step))
        formatted          (f/unparse (f/formatters :date-time) most-recent-update)]
    formatted))

(defn- project-for [context config fallback-base-url step-info]
  (let [step-id           (:step-id step-info)
        formatted-step-id (s/join "-" step-id)
        states-for-step   (states-for step-id context)
        state-for-step    (first states-for-step)
        last-build-number (:build-number state-for-step)
        pipeline-name     (:name config)
        add-prefix        (get config :cctray-add-prefix true)
        step-name         (:name step-info)
        name              (if (and add-prefix pipeline-name)
                            (str pipeline-name " :: " step-name)
                            step-name)
        base-url          (or (:ui-url config) fallback-base-url)]
    (xml/element :Project {:name            name
                           :activity        (:activity state-for-step)
                           :lastBuildStatus (last-build-status-for states-for-step)
                           :lastBuildLabel  (str last-build-number)
                           :lastBuildTime   (last-build-time-for state-for-step)
                           :webUrl          (str base-url "/#/builds/" last-build-number "/" formatted-step-id)} [])))

(defn- flatten-pipeline [pipeline-representation]
  (let [children-reps (flatten (map #(flatten-pipeline (:children %)) pipeline-representation))]
    (concat pipeline-representation children-reps)))

(defn- projects-for [pipeline fallback-base-url]
  (let [def                     (:pipeline-def pipeline)
        context                 (:context pipeline)
        config                  (:config context)
        state-component         (:pipeline-state-component context)
        pipeline-representation (flatten-pipeline (lp/pipeline-display-representation def))]
    (map (partial project-for context config fallback-base-url) pipeline-representation)))

(defn cctray-xml-for
  ([pipeline]
   (cctray-xml-for pipeline nil))
  ([pipelines fallback-base-url]
   (let [pipelines (if (map? pipelines) [pipelines] pipelines)]
     (xml/emit-str
       (xml/element :Projects {} (flatten (map #(projects-for % fallback-base-url) pipelines)))))))

(defn cctray-handler-for
  ([pipeline]
   (cctray-handler-for pipeline nil))
  ([pipeline fallback-base-url]
   (fn [& _]
     {:status  200
      :headers {"Content-Type" "application/xml"}
      :body    (cctray-xml-for pipeline fallback-base-url)})))

