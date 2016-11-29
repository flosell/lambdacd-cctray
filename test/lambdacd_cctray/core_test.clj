(ns lambdacd-cctray.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-cctray.core :as parser]
            [clj-time.core :as t]
            [lambdacd.steps.control-flow :as control-flow]
            [lambdacd.state.protocols :as protocols]
            [lambdacd-cctray.core :refer :all]))

(defn some-name [& _])
(defn some-other-name [& _])
(defn some-other-other-name [& _])

(def pipeline-def
  `(
     some-name
     (control-flow/either
       some-other-name
       some-other-other-name)))

(def some-state {8 {[1] {:status                :success
                         :first-updated-at      (t/date-time 2015 1 2 3 40 1)
                         :most-recent-update-at (t/date-time 2015 1 2 3 40 0)}}
                 3 {[1 2] {:status                :running
                           :first-updated-at      (t/date-time 2015 1 2 3 40 3)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 3)}
                    [2 2] {:status                :waiting
                           :first-updated-at      (t/date-time 2015 1 2 3 40 3)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 3)}}
                 2 {[1 2] {:status                :failure
                           :first-updated-at      (t/date-time 2015 1 2 3 40 2)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 2)}
                    [2 2] {:status                :success
                           :first-updated-at      (t/date-time 2015 1 2 3 40 2)
                           :most-recent-update-at (t/date-time 2015 1 2 3 40 2)}}})

(defrecord MockStateComponent [state]
  protocols/QueryAllBuildNumbersSource
  (all-build-numbers [self]
    (keys state))
  protocols/QueryStepResultsSource
  (get-step-results [self build-number]
    (get state build-number))) ; TODO: also implement pipeline structure

(defn mock-state-component [state]
  (->MockStateComponent state))

(defn mock-pipeline [state config]
  {:pipeline-def pipeline-def
   :context      {:pipeline-state-component (mock-state-component state) :config config}})

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml w/o name and ui-url"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state {}) "some/base/url")
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
              :activity          :building
              :last-build-status :failure
              :last-build-label  "3"
              :last-build-time   (t/date-time 2015 1 2 3 40 3)
              :web-url           "some/base/url/#/builds/3/1-2"
              :messages          []
              :next-build-time   nil
              :prognosis         :sick-building} (nth projects 2)))
      (is (= {:name              "some-other-other-name"
              :activity          :sleeping
              :last-build-status :success
              :last-build-label  "3"
              :last-build-time   (t/date-time 2015 1 2 3 40 3)
              :web-url           "some/base/url/#/builds/3/2-2"
              :messages          []
              :next-build-time   nil
              :prognosis         :healthy} (nth projects 3)))))
  (testing "That it produces a valid cctray-xml w/ name"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state {:name   "some-crazy-pipeline"}) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"
              "some-crazy-pipeline :: some-other-other-name"
              ]
             names))))
  (testing "That it produces a valid cctray-xml for multiple pipelines"
    (let [xmlstring (cctray-xml-for [(mock-pipeline some-state {:name   "some-crazy-pipeline"
                                                                :ui-url "some/crazy/url"})
                                     (mock-pipeline some-state {:name   "some-other-pipeline"
                                                                :ui-url "some/other/url"})]
                                    "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"
              "some-crazy-pipeline :: some-other-other-name"
              "some-other-pipeline :: some-name"
              "some-other-pipeline :: either"
              "some-other-pipeline :: some-other-name"
              "some-other-pipeline :: some-other-other-name"]
             (map :name projects)))
      (is (= ["some/crazy/url/#/builds/8/1"
              "some/crazy/url/#/builds//2"
              "some/crazy/url/#/builds/3/1-2"
              "some/crazy/url/#/builds/3/2-2"
              "some/other/url/#/builds/8/1"
              "some/other/url/#/builds//2"
              "some/other/url/#/builds/3/1-2"
              "some/other/url/#/builds/3/2-2"
              ]
             (map :web-url projects)))))
  (testing "That it produces a valid cctray-xml with explicit enabled prefix"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state {:name   "some-crazy-pipeline"
                                                               :cctray-add-prefix true}) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"
              "some-crazy-pipeline :: some-other-other-name"]
             names))))
  (testing "That it produces a valid cctray-xml with disabled prefix"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state {:name   "some-crazy-pipeline"
                                                               :cctray-add-prefix false}) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-name"
              "either"
              "some-other-name"
              "some-other-other-name"]
             names)))))
