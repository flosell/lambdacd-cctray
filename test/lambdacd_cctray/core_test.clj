(ns lambdacd-cctray.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-cctray.core :as parser]
            [clj-time.core :as t]
            [lambdacd.control-flow :as control-flow]
            [lambdacd-cctray.core :refer :all]))

(defn some-name [& _])
(defn some-other-name [& _])

(def pipeline-def
  `(
     some-name
     (control-flow/either
       some-other-name)))

(def some-state {8 { [1] {:status :success}}
                 3 { [1 2] {:status :running}}})

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml"
    (let [xmlstring (cctray-xml-for pipeline-def some-state)
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)]
      (is (= {:name "some-name"
              :activity :sleeping
              :last-build-status :exception
              :last-build-label "8" ;; maybe this should be another build?
              :last-build-time (t/date-time 2005 9 28 9 30 34 0)
              :web-url "some-host/some-path/"
              :messages          []
              :next-build-time   nil
              :prognosis         :unknown} (first projects)))
      (is (= {:name "some-other-name"
               :activity :building
               :last-build-status :exception
               :last-build-label "3" ;; maybe this should be another build?
               :last-build-time (t/date-time 2005 9 28 9 30 34 0)
               :web-url "some-host/some-path/"
               :messages          []
               :next-build-time   nil
               :prognosis         :unknown} (nth projects 2))))))
