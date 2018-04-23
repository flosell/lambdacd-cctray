(ns lambdacd-cctray.sample-pipeline
  (:use [compojure.core]
        [lambdacd.steps.control-flow])
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.steps.manualtrigger :as manualtrigger]
            [lambdacd.core :as lambdacd]
            [ring.server.standalone :as ring-server]
            [lambdacd.ui.core :as ui]
            [lambdacd.runners :as runners]
            [lambdacd-cctray.core :as cctray]
            [ring.util.response :as resp])
  (:import (java.nio.file.attribute FileAttribute)
           (java.nio.file Files)))

(defn some-slow-step [args ctx]
  (shell/bash ctx "/"
              "echo hello"
              "sleep 1"
              "echo hello world"
              "sleep 2"
              "echo still here"
              "sleep 10"
              "echo ok im done"))


(defn some-failing-step [args ctx]
  (shell/bash ctx "/"
              "exit 1"))

(defn some-successful-step [args ctx]
  (shell/bash ctx "/"
              "echo yay"
              "exit 0"))

(defn wait-for-interaction [args ctx]
  (manualtrigger/wait-for-manual-trigger nil ctx))

(def pipeline-structure `(
                           some-slow-step
                           wait-for-interaction
                           (either
                             some-failing-step
                             some-successful-step)))

(defn create-temp-dir []
  (str (Files/createTempDirectory "lambdacd-cctest-sample" (into-array FileAttribute []))))

(defn mk-routes [pipeline-routes cctray-pipeline-handler]
  (routes
    (GET "/" [] (resp/redirect "pipeline/"))
    (context "/pipeline" [] pipeline-routes)
    (GET "/cctray/pipeline.xml" [] cctray-pipeline-handler)))

(defn -main [& args]
  (let [home-dir (if (not (empty? args)) (first args) (create-temp-dir))
        config {:home-dir home-dir
                :name     "some sample pipeline"
                :ui-url   "http://localhost:8081/pipeline"}
        pipeline (lambdacd/assemble-pipeline pipeline-structure config)
        cctray-pipeline-handler (cctray/cctray-handler-for pipeline)]
    (runners/start-one-run-after-another pipeline)
    (ring-server/serve (mk-routes (ui/ui-for pipeline) cctray-pipeline-handler)
                       {:open-browser? true
                        :port          8081})))
