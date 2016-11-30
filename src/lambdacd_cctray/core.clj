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
  (if-let [most-recent-update (:most-recent-update-at (:result step))]
    (f/unparse (f/formatters :date-time) most-recent-update)))

(defn web-url [context build-number step-id]
  (let [base-url          (:ui-url (:config context))
        formatted-step-id (s/join "-" step-id)]
    (str base-url "/#/builds/" (or build-number -1) "/" formatted-step-id)))

(defn- step-name [context step-info]
  (let [config        (:config context)
        pipeline-name (:name config)
        add-prefix    (get config :cctray-add-prefix true)
        step-name     (:name step-info)]
    (if (and add-prefix pipeline-name)
      (str pipeline-name " :: " step-name)
      step-name)))

(defn- project-for [context step-info]
  (let [step-id                (:step-id step-info)
        states-for-step        (states-for step-id context)
        state-for-current-step (first states-for-step)
        current-build-number   (:build-number state-for-current-step)]
    (xml/element :Project {:name            (step-name context step-info)
                           :activity        (or (:activity state-for-current-step) "Sleeping")
                           :lastBuildStatus (last-build-status-for states-for-step)
                           :lastBuildLabel  (str (or current-build-number "unknown"))
                           :lastBuildTime   (last-build-time-for state-for-current-step)
                           :webUrl          (web-url context current-build-number step-id)} [])))

(defn- flatten-pipeline [pipeline-representation]
  (let [children-reps (flatten (map #(flatten-pipeline (:children %)) pipeline-representation))]
    (concat pipeline-representation children-reps)))

(defn- projects-for [pipeline]
  (for [step-info (flatten-pipeline (lp/pipeline-display-representation (:pipeline-def pipeline)))]
    (project-for (:context pipeline) step-info)))

(defn cctray-xml-for
  ([pipelines]
   (let [pipelines (if (map? pipelines) [pipelines] pipelines)]
     (xml/emit-str
       (xml/element :Projects {} (flatten (map #(projects-for %) pipelines)))))))

(defn cctray-handler-for
  ([pipeline]
   (fn [& _]
     {:status  200
      :headers {"Content-Type" "application/xml"}
      :body    (cctray-xml-for pipeline)})))

