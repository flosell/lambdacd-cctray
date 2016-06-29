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

(defn mock-pipeline [state name ui-url cctray-add-prefix]
  {:pipeline-def pipeline-def
   :context      {:pipeline-state-component (mock-state-component state) :config {:name   name
                                                                                  :ui-url ui-url
                                                                                  :cctray-add-prefix cctray-add-prefix}}})

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml w/o name and ui-url"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state nil nil nil) "some/base/url")
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
              :prognosis         :sick} (nth projects 2)))))
  (testing "That it produces a valid cctray-xml w/ name"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state "some-crazy-pipeline" nil nil) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"]
             names))))
  (testing "That it produces a valid cctray-xml for multiple pipelines"
    (let [xmlstring (cctray-xml-for [(mock-pipeline some-state "some-crazy-pipeline" "some/crazy/url" nil)
                                     (mock-pipeline some-state "some-other-pipeline" "some/other/url" nil)]
                                    "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"
              "some-other-pipeline :: some-name"
              "some-other-pipeline :: either"
              "some-other-pipeline :: some-other-name"]
             (map :name projects)))
      (is (= ["some/crazy/url/#/builds/8/1"
              "some/crazy/url/#/builds//2"
              "some/crazy/url/#/builds/2/1-2"
              "some/other/url/#/builds/8/1"
              "some/other/url/#/builds//2"
              "some/other/url/#/builds/2/1-2"]
             (map :web-url projects)))))
  (testing "That it produces a valid cctray-xml with explicit enabled prefix"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state "some-crazy-pipeline" nil true) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-crazy-pipeline :: some-name"
              "some-crazy-pipeline :: either"
              "some-crazy-pipeline :: some-other-name"]
             names))))
  (testing "That it produces a valid cctray-xml with disabled prefix"
    (let [xmlstring (cctray-xml-for (mock-pipeline some-state "some-crazy-pipeline" nil false) "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)
          names (map :name projects)]
      (is (= ["some-name"
              "either"
              "some-other-name"]
             names)))))
