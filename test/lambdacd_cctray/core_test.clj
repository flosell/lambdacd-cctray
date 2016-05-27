(ns lambdacd-cctray.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-cctray.core :as parser]
            [clj-time.core :as t]
            [lambdacd.steps.control-flow :as control-flow]
            [lambdacd.internal.pipeline-state :as pipeline-state]
            [lambdacd-cctray.core :refer :all]))

(defn some-name [& _])
(defn some-other-name [& _])

(def pipeline-def
  `(
     some-name
     (control-flow/either
       some-other-name)))

(def some-state {8 {[1] {:status                :success
                         :first-updated-at      (t/date-time 2015 1 2 3 40 1)
                         :most-recent-update-at (t/date-time 2015 1 2 3 40 0)}}
                 3 {[1 2] {:status                :running
                           :first-updated-at      (t/date-time 2015 1 2 3 40 1)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 1)}}
                 2 {[1 2] {:status                :failure
                           :first-updated-at      (t/date-time 2015 1 2 3 40 2)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 2)}}})

(defn mock-state-component [state]
  (reify pipeline-state/PipelineStateComponent
    (get-all [self] state)))

(defn mock-pipeline [state]
  {:pipeline-def pipeline-def
   :context      {:pipeline-state-component (mock-state-component state)}})

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)]
      (is (= {:name              "some-name"
              :activity          :sleeping
              :last-build-status :success
              :last-build-label  "8"
              :last-build-time   (t/date-time 2015 1 2 3 40 0)
              :web-url           "some/base/url/#/builds/8/1"
              :messages          []
              :next-build-time   nil
              :prognosis         :healthy} (first projects)))
      (is (= {:name              "some-other-name"
              :activity          :sleeping
              :last-build-status :failure
              :last-build-label  "2"
              :last-build-time   (t/date-time 2015 1 2 3 40 2)
              :web-url           "some/base/url/#/builds/2/1-2"
              :messages          []
              :next-build-time   nil
              :prognosis         :sick} (nth projects 2))))))
