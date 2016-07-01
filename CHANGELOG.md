# Changelog

## 0.4.2

* Add option `cctray-add-prefix` to disable or enable (default) prefixing step names with pipeline name

## 0.4.1

* Support multiple pipelines.
* Support prefixing builds with pipeline name from config map.

* API Changes:
  * base-url now configured through pipelines config map.
  * Deprecate base-url as argument for `cctray-xml-for` and `cctray-handler-for`.

## 0.4.0

* Support LambdaCD versions 0.5.1 (#4). Earlier versions of LambdaCD are no longer supported.

## 0.3.0

* API Changes:
  * Changed interface for `cctray-xml-for` to match that of `cctray-handler-for`:

    ```clojure
    (:require [lambdacd-cctray.core :as cctray])

    (let [pipeline (lambdacd/assemble-pipeline pipeline/pipeline-def {})
          cctray-xml (cctray/cctray-xml-for pipeline "http://some-base-url")
    ```


## 0.2.0

* Support LambdaCD versions > 0.5.0 (#2).
  Older versions are no longer supported but versions down to 0.4.1 might still work.
* Determine most recent build by step start date instead of build number to ensure proper behavior when retriggering (#3)
* API Changes: 
  * Removed deprecated `(cctray-handler-for pipeline-def state-atom base-ur)`. Use the interface as shown in README

## 0.1.4

* New signature for main entry-point `cctray-handler-for`: Now expects the pipeline-instance returned by
  `lambdacd.core/assemble-pipeline`.
  The previous interface expecting state and pipeline definition as parameters still works but is deprecated
  and will be removed in subsequent releases. 