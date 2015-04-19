# Changelog

## 0.1.4

* New signature for main entry-point `cctray-handler-for`: Now expects the pipeline-instance returned by
  `lambdacd.core/assemble-pipeline`.
  The previous interface expecting state and pipeline definition as parameters still works but is deprecated
  and will be removed in subsequent releases. 