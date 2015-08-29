# Changelog

## 0.2.0

* Support LambdaCD versions > 0.5.0. Older versions are no longer supported but versions down to 0.4.1 might still work.
* API Changes: 
  * Removed deprecated `(cctray-handler-for pipeline-def state-atom base-ur)`. Use the interface as shown in README

## 0.1.4

* New signature for main entry-point `cctray-handler-for`: Now expects the pipeline-instance returned by
  `lambdacd.core/assemble-pipeline`.
  The previous interface expecting state and pipeline definition as parameters still works but is deprecated
  and will be removed in subsequent releases. 