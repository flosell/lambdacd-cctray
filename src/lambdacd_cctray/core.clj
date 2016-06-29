(ns lambdacd-cctray.core
  (:require [clojure.data.xml :as xml]
            [lambdacd.presentation.pipeline-structure :as lp]
            [lambdacd.internal.pipeline-state :as pipeline-state]
            [clojure.string :as s]
            [clj-time.format :as f]))

(defn- has-step-id [step-id [_ steps]]
  (get steps step-id))

(defn- state-for [step-id [k steps]]
  (let [build-number k
        status (get steps step-id)
        activity (if (= (:status status) :running) "Building" "Sleeping")]
    {:build-number build-number
     :activity     activity
     :result       status}))

(defn- first-updated [step-id]
  (fn [[_ steps]]
    (:first-updated-at (get steps step-id))))

(defn- states-for [step-id state]
  (let [builds-with-step-id (filter #(has-step-id step-id %) (seq state))
        by-most-recent (reverse (sort-by (first-updated step-id) builds-with-step-id))]
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

(defn- last-build-time-for [step]
  (let [most-recent-update (:most-recent-update-at (:result step))
        formatted (f/unparse (f/formatters :date-time) most-recent-update)]
    formatted))

(defn- get-add-prefix-with-default [config]
  (let [enabled (:cctray-add-prefix config)]
    (if (nil? enabled)
      true
      enabled)))

(defn- project-for [state config fallback-base-url step-info]
  (let [step-id (:step-id step-info)
        formatted-step-id (s/join "-" step-id)
        states-for-step (states-for step-id state)
        state-for-step (first states-for-step)
        last-build-number (:build-number state-for-step)
        pipeline-name (:name config)
        add-prefix (get-add-prefix-with-default config)
        step-name (:name step-info)
        name (if (and add-prefix pipeline-name)
               (str pipeline-name " :: " step-name)
               step-name)
        base-url (or (:ui-url config) fallback-base-url)]
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
  (let [def (:pipeline-def pipeline)
        context (:context pipeline)
        config (:config context)
        state-component (:pipeline-state-component context)
        state (pipeline-state/get-all state-component)
        pipeline-representation (flatten-pipeline (lp/pipeline-display-representation def))]
    (map (partial project-for state config fallback-base-url) pipeline-representation)))

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

