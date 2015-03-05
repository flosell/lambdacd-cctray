# CCTray support for LambdaCD


## Status

Work in progress

[![Build Status](https://travis-ci.org/flosell/lambdacd-cctray.svg)](https://travis-ci.org/flosell/lambdacd-cctray)

## Usage

`[lambdacd-cctray "0.1.0-SNAPSHOT"]` (not yet on clojars, you'll have to build it yourself)

```clojure
(let [pipeline (lambdacd/mk-pipeline pipeline/pipeline-def {})
      cctray-pipeline-handler (cctray/cctray-handler-for pipeline/pipeline-def (:state pipeline))
  ; ...
  (GET "/cctray/pipeline.xml" [] cctray-pipeline-handler)
```



## License

Copyright © 2014 Florian Sellmayr

Distributed under the Apache License 2.0
