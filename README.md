# CCTray support for LambdaCD
Exposes a `cctray.xml` for your [LambdaCD](https://github.com/flosell/lambdacd) pipeline to integrate with build monitoring tools such as [nevergreen](http://nevergreen.io/) or [CCMenu](http://ccmenu.org/)

## Status

[![Build Status](https://travis-ci.org/flosell/lambdacd-cctray.svg)](https://travis-ci.org/flosell/lambdacd-cctray)

[![Clojars Project](http://clojars.org/lambdacd-cctray/latest-version.svg)](http://clojars.org/lambdacd-cctray)
## Usage

### Single pipeline

```clojure
(:require [lambdacd-cctray.core :as cctray])

(let [pipeline (lambdacd/assemble-pipeline pipeline/pipeline-def {:ui-url "http://some-base-url"})
      cctray-pipeline-handler (cctray/cctray-handler-for pipeline)

  ; ...

  (GET "/cctray/pipeline.xml" [] cctray-pipeline-handler)
```

For a full example, see [test/lambdacd_cctray/sample_pipeline.clj](test/lambdacd_cctray/sample_pipeline.clj)

### Multiple pipelines

```clojure
(:require [lambdacd-cctray.core :as cctray])

(let [some-pipeline (lambdacd/assemble-pipeline pipeline/some-pipeline-def {:ui-url "http://some-base-url/some-pipeline"})
      some-other-pipeline (lambdacd/assemble-pipeline pipeline/some-other-pipeline-def {:ui-url "http://some-base-url/some-other-pipeline"})
      cctray-pipeline-handler (cctray/cctray-handler-for [some-pipeline some-other-pipeline])

  ; ...

  (GET "/cctray/pipeline.xml" [] cctray-pipeline-handler)
```

### Prefixing

By default the step names in the resulting xml will be prefixed with the corresonding pipeline name.
This can be disabled with the key `cctray-add-prefix` in the pipeline config:
 ```clojure
 (let [some-pipeline (lambdacd/assemble-pipeline pipeline/some-pipeline-def {:ui-url "http://some-base-url/some-pipeline"
                                                                             :cctray-add-prefix false})
 ...
 ```

## Development

Call `./go`

## License

Copyright © 2014 Florian Sellmayr

Distributed under the Apache License 2.0
