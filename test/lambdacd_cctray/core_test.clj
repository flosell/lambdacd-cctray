(ns lambdacd-cctray.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-cctray.core :as parser]
            [clj-time.core :as t]
            [lambdacd-cctray.core :refer :all]))

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml"
    (let [xmlstring (cctray-xml-for nil nil)
          xmlstream (io/input-stream (.getBytes xmlstring))]
      (is (= [{:name "Some Name"
              :activity :sleeping
              :last-build-status :exception
              :last-build-label "8"
              :last-build-time (t/date-time 2005 9 28 9 30 34 0)
              :web-url "some-host/some-path/"
              :messages          []
              :next-build-time   nil
              :prognosis         :unknown}] (parser/get-projects xmlstream))))))
