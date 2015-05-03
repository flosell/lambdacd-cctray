# CCTray support for LambdaCD
Exposes a `cctray.xml` for your [LambdaCD](https://github.com/flosell/lambdacd) pipeline to integrate with build monitoring tools such as [nevergreen](http://nevergreen.io/) or [CCMenu](http://ccmenu.org/)

## Status

[![Build Status](https://travis-ci.org/flosell/lambdacd-cctray.svg)](https://travis-ci.org/flosell/lambdacd-cctray)

[![Clojars Project](http://clojars.org/lambdacd-cctray/latest-version.svg)](http://clojars.org/lambdacd-cctray)
## Usage

```clojure
(:require [lambdacd-cctray.core :as cctray])

(let [pipeline (lambdacd/assemble-pipeline pipeline/pipeline-def {})
      cctray-pipeline-handler (cctray/cctray-handler-for pipeline "http://some-base-url")

  ; ...

  (GET "/cctray/pipeline.xml" [] cctray-pipeline-handler)
```

For a full example, see [test/lambdacd_cctray/sample_pipeline.clj](test/lambdacd_cctray/sample_pipeline.clj)

## Development

Call `./go`

## License

Copyright © 2014 Florian Sellmayr

Distributed under the Apache License 2.0
